package com.dractical.femutils.core.config;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClassIntrospector {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<Class<?>, ClassMeta> CACHE = new ConcurrentHashMap<>();

    private ClassIntrospector() {
    }

    public static ClassMeta get(Class<?> type) {
        return CACHE.computeIfAbsent(type, ClassIntrospector::build);
    }

    private static ClassMeta build(Class<?> type) {
        if (type.isRecord()) {
            return buildRecord(type);
        }
        return buildPojo(type);
    }

    private static ClassMeta buildRecord(Class<?> type) {
        try {
            RecordComponent[] comps = type.getRecordComponents();
            Class<?>[] ctorTypes = new Class<?>[comps.length];
            for (int i = 0; i < comps.length; i++) {
                ctorTypes[i] = comps[i].getType();
            }
            Constructor<?> ctor = type.getDeclaredConstructor(ctorTypes);
            ctor.setAccessible(true);

            List<RecordProperty> props = new ArrayList<>(comps.length);
            for (RecordComponent rc : comps) {
                MethodHandle getter = LOOKUP.unreflect(rc.getAccessor());
                Comment comment = rc.getAccessor().getAnnotation(Comment.class);
                props.add(new RecordProperty(
                        rc.getName(),
                        rc.getType(),
                        rc.getGenericType(),
                        getter,
                        comment
                ));
            }
            Header header = type.getAnnotation(Header.class);
            return ClassMeta.forRecord(type, header, ctor, props);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static ClassMeta buildPojo(Class<?> type) {
        Constructor<?> ctor;
        try {
            ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            ctor = null;
        }

        List<PojoField> fields = new ArrayList<>();
        for (Field f : type.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            VarHandle vh = null;
            try {
                vh = MethodHandles.privateLookupIn(type, LOOKUP).unreflectVarHandle(f);
            } catch (IllegalAccessException ignored) {
            }
            Comment c = f.getAnnotation(Comment.class);
            fields.add(new PojoField(
                    f.getName(),
                    f.getType(),
                    f.getGenericType(),
                    f,
                    vh,
                    c
            ));
        }
        Header header = type.getAnnotation(Header.class);
        return ClassMeta.forPojo(type, header, ctor, fields);
    }

    public record ClassMeta(
            Class<?> type,
            Header header,
            boolean isRecord,
            Constructor<?> ctor,
            List<RecordProperty> recordProps,
            List<PojoField> pojoFields
    ) {
        static ClassMeta forRecord(Class<?> type, Header header, Constructor<?> ctor, List<RecordProperty> props) {
            return new ClassMeta(type, header, true, ctor, List.copyOf(props), List.of());
        }

        static ClassMeta forPojo(Class<?> type, Header header, Constructor<?> ctor, List<PojoField> fields) {
            return new ClassMeta(type, header, false, ctor, List.of(), List.copyOf(fields));
        }
    }

    public record RecordProperty(
            String name,
            Class<?> rawType,
            Type genericType,
            MethodHandle getter,
            Comment comment
    ) {
    }

    public record PojoField(
            String name,
            Class<?> rawType,
            Type genericType,
            Field reflectField,
            VarHandle handle,
            Comment comment
    ) {
    }
}
