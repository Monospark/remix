package org.monospark.remix.internal;

import org.monospark.remix.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class RecordBuilderImpl<R extends Record> implements RecordBuilder<R> {

    private Class<R> recordClass;
    private RecordCacheData<R> cacheData;
    private Map<RecordParameter, Supplier<?>> mapping;

    RecordBuilderImpl(Class<R> recordClass, Map<RecordParameter, Supplier<?>> mapping) {
        this.cacheData = RecordCache.getOrAdd(recordClass);
        this.recordClass = recordClass;
        this.mapping = new HashMap<>(mapping);
    }

    public RecordBuilderImpl(Class<R> recordClass) {
        this.cacheData = RecordCache.getOrAdd(recordClass);
        this.recordClass = recordClass;
        this.mapping = new HashMap<>();
    }

    public R build() {
        Object[] args = new Object[cacheData.getParameters().size()];
        int i = 0;
        for (RecordParameter p : cacheData.getParameters()) {
            if (!mapping.containsKey(p)) {
                args[i] = p.defaultValue();
            } else {
                args[i] = mapping.get(p).get();
            }
            i++;
        }
        try {
            return Records.create(recordClass, args);
        } catch (Exception e) {
            throw new RecordBuilderException("Could not create record instance", e);
        }
    }

    @Override
    public <T extends R> RecordBlank<T> blank() {
        return new RecordBlankImpl<T>((Class<T>) recordClass, mapping);
    }

    @Override
    public <T> ComponentContext<R, T> set(Function<R, T> component) {
        return new ComponentContext<R, T>() {
            @Override
            public RecordBuilder<R> to(Supplier<T> value) {
                RecordResolver.ParameterValue<T> result = cacheData.getResolverCache().resolve(component, value);
                if (result != null) {
                    RecordBuilderImpl.this.mapping.put(result.parameter(), value);
                }
                return RecordBuilderImpl.this;
            }
        };
    }

    @Override
    public <T> ComponentContext<R, T> set(LambdaSupport.WrappedFunction<R, T> component) {
        return new ComponentContext<R, T>() {
            @Override
            public RecordBuilder<R> to(Supplier<T> value) {
                RecordParameter param = cacheData.getResolverCache().resolveWrapped(component);
                RecordBuilderImpl.this.mapping.put(param, value);
                return RecordBuilderImpl.this;
            }
        };
    }
}
