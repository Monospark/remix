package org.monospark.remix.internal;

import org.monospark.remix.RemixAnnotationException;
import org.monospark.remix.defaults.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DefaultAnnotationType {

    private Annotation annotation;
    private boolean isFactory;
    private Object defaultValue;
    private Groups.Factory group;

    private static final List<Class<? extends Annotation>> DEFAULT_ANNOTATIONS =
            List.of(Default.class, DefaultBoolean.class, DefaultLong.class, DefaultInt.class, DefaultString.class);

    static DefaultAnnotationType fromRecordComponent(RecordComponent c) {
        Optional<Annotation> anno = Arrays.stream(c.getAnnotations())
                .filter(a -> DEFAULT_ANNOTATIONS.contains(a.annotationType()))
                .findFirst();
        if (anno.isPresent()) {
            Annotation annotation = anno.get();
            try {
                Object array = annotation.annotationType().getField("value").get(annotation);
                boolean isGroup = Array.getLength(array) > 1;

                Object defaultValue = array;
                Groups.Factory group = null;
                if (isGroup) {
                    group = Groups.getFactory((Class<?>) annotation.annotationType().getField("group").get(annotation));
                } else {
                    defaultValue = array;
                }
                boolean isFactory = Defaults.Factory.class.isAssignableFrom(Array.get(defaultValue, 0).getClass());
                if (!isFactory) {
                    defaultValue = isGroup ? group.group(defaultValue) : Array.get(defaultValue, 0);
                }

                return new DefaultAnnotationType(annotation, isFactory, defaultValue, group);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RemixAnnotationException(e);
            }
        }
        return null;
    }

    private DefaultAnnotationType(Annotation annotation, boolean isFactory, Object defaultValue, Groups.Factory group) {
        this.annotation = annotation;
        this.isFactory = isFactory;
        this.defaultValue = defaultValue;
        this.group = group;
    }

    public Object defaultObject(Class<?> clazz) {
        if (isFactory) {
            Class<? extends Defaults.Factory>[] factories = (Class<? extends Defaults.Factory>[]) defaultValue;

            Object[] v = new Object[factories.length];
            for (var f : factories) {
                Defaults.getFactory(f).create(clazz);
            }
            return group != null ? group.group(v) : Array.get(v, 0);
        } else {
            return defaultValue;
        }
    }

    public int defaultInt() {
        return Array.getInt(defaultValue, 0);
    }

    public boolean defaultBoolean() {
        return Array.getBoolean(defaultValue, 0);
    }
}
