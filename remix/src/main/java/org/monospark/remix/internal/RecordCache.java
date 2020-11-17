package org.monospark.remix.internal;

import org.monospark.remix.RecordRemixer;

import java.util.HashMap;
import java.util.Map;

public class RecordCache {

    private static final Map<Class<?>, RecordCacheData<?>> DATA = new HashMap<>();


    public static <T extends Record> RecordCacheData<T> getOrAdd(Class<T> recordClass) {
        if (DATA.containsKey(recordClass)) {
            return (RecordCacheData<T>) DATA.get(recordClass);
        } else {
            RecordCacheData<T> r = RecordCacheData.fromRecordClass(recordClass, null);
            DATA.put(recordClass, r);
            return r;
        }
    }

    public static <T extends Record> void register(Class<T> recordClass, RecordRemixer<T> rm) {
        RecordCacheData<T> r = RecordCacheData.fromRecordClass(recordClass, rm);
        DATA.put(recordClass, r);
    }
}