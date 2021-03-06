package org.monospark.remix.internal;

import org.monospark.remix.*;

import java.util.Map;

public abstract class RecordComponentType {

    private static final Map<Class<?>, RecordComponentType> TYPES = Map.of(
            Wrapped.class, new WrappedObjectType(),
            WrappedBoolean.class, new WrappedBooleanType(),
            WrappedInt.class, new WrappedIntType(),
            Mutable.class, new MutableType(),
            MutableBoolean.class, new MutableBooleanType());

    static RecordComponentType get(Class<?> type) {
        return TYPES.getOrDefault(type, new BareType(type));
    }


    abstract boolean isMutable();

    abstract boolean isWrapped();

    public abstract Object wrap(RecordParameter param, Object value);

    public abstract Object unwrap(Object value);

    public abstract Object defaultValue(RecordParameter param);

    public abstract Class<?> getValueType();

    public static final class BareType extends RecordComponentType {

        private Class<?> clazz;

        public BareType(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        boolean isMutable() {
            return false;
        }

        @Override
        boolean isWrapped() {
            return false;
        }

        @Override
        public Object wrap(RecordParameter param, Object value) {
            return value;
        }

        @Override
        public Object unwrap(Object value) {
            return value;
        }

        @Override
        public Object defaultValue(RecordParameter param) {
            return DefaultValueHelper.createDefaultValue(clazz);
        }

        @Override
        public Class<?> getValueType() {
            return clazz;
        }
    }

    public static class WrappedBooleanType extends RecordComponentType {

        @Override
        boolean isMutable() {
            return false;
        }

        @Override
        boolean isWrapped() {
            return true;
        }

        @Override
        public Object wrap(RecordParameter param, Object value) {
            return new WrappedBooleanImpl(param, (boolean) value);
        }

        @Override
        public Object unwrap(Object value) {
            return ((WrappedBooleanImpl) value).value;
        }

        @Override
        public Object defaultValue(RecordParameter param) {
            return false;
        }

        @Override
        public Class<?> getValueType() {
            return boolean.class;
        }
    }

    public static final class MutableBooleanType extends WrappedBooleanType {
        @Override
        boolean isMutable() {
            return true;
        }

        @Override
        public Object wrap(RecordParameter param, Object value) {
            return new MutableBooleanImpl(param, (boolean) value);
        }
    }

    public static final class WrappedIntType extends RecordComponentType {

        @Override
        boolean isMutable() {
            return false;
        }

        @Override
        boolean isWrapped() {
            return true;
        }

        @Override
        public Object unwrap(Object value) {
            return ((WrappedIntImpl) value).getInt();
        }

        @Override
        public Object wrap(RecordParameter param, Object value) {
            return new WrappedIntImpl(param, (int) value);
        }

        @Override
        public Object defaultValue(RecordParameter param) {
            return 0;
        }

        @Override
        public Class<Integer> getValueType() {
            return int.class;
        }
    }

    public static class WrappedObjectType extends RecordComponentType {

        @Override
        boolean isMutable() {
            return false;
        }

        @Override
        boolean isWrapped() {
            return true;
        }

        @Override
        public Object unwrap(Object value) {
            return ((WrappedImpl<?>) value).get();
        }

        @Override
        public Object wrap(RecordParameter param, Object value) {
            return new WrappedImpl<>(param, value);
        }

        @Override
        public Object defaultValue(RecordParameter param) {
            return null;
        }

        @Override
        public Class<?> getValueType() {
            return Object.class;
        }
    }

    public static final class MutableType extends WrappedObjectType {

        @Override
        boolean isMutable() {
            return true;
        }

        @Override
        public Object unwrap(Object value) {
            return ((MutableImpl<?>) value).get();
        }

        @Override
        public Object wrap(RecordParameter param, Object value) {
            return new MutableImpl<>(param, value);
        }
    }
}
