package com.dractical.femutils.core.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class ReflectMapper {
    private final TypeRegistry registry;

    public ReflectMapper(TypeRegistry registry) {
        this.registry = registry;
    }

    public <T> T toObject(Object raw, Class<T> type) {
        if (type.isPrimitive()) {
            if (raw == null) {
                if (type == boolean.class) return (T) Boolean.FALSE;
                if (type == byte.class) return (T) Byte.valueOf((byte) 0);
                if (type == short.class) return (T) Short.valueOf((short) 0);
                if (type == int.class) return (T) Integer.valueOf(0);
                if (type == long.class) return (T) Long.valueOf(0L);
                if (type == float.class) return (T) Float.valueOf(0f);
                if (type == double.class) return (T) Double.valueOf(0d);
                if (type == char.class) return (T) Character.valueOf('\0');
            }
            if (type == boolean.class) {
                Boolean boolVal = (raw instanceof Boolean b)
                        ? b
                        : Boolean.valueOf(Boolean.parseBoolean(raw.toString()));
                return (T) boolVal;
            }
            if (raw instanceof Number num) {
                if (type == byte.class) return (T) Byte.valueOf(num.byteValue());
                if (type == short.class) return (T) Short.valueOf(num.shortValue());
                if (type == int.class) return (T) Integer.valueOf(num.intValue());
                if (type == long.class) return (T) Long.valueOf(num.longValue());
                if (type == float.class) return (T) Float.valueOf(num.floatValue());
                if (type == double.class) return (T) Double.valueOf(num.doubleValue());
            }
            if (type == char.class && raw instanceof String s && !s.isEmpty()) {
                return (T) Character.valueOf(s.charAt(0));
            }
            return (T) raw;
        }

        if (raw == null) {
            return null;
        }

        if (type == Boolean.class
                || type == Byte.class
                || type == Short.class
                || type == Integer.class
                || type == Long.class
                || type == Float.class
                || type == Double.class
                || type == Character.class
                || type == String.class
        ) {
            return (T) raw;
        }

        if (type == Object.class || type.isInstance(raw)) {
            return (T) raw;
        }

        if (type.isEnum()) {
            if (type.isInstance(raw)) {
                return type.cast(raw);
            }
            String text = raw.toString().trim();
            if (text.isEmpty()) {
                return null;
            }
            Class<? extends Enum> enumType = (Class<? extends Enum>) type;
            return (T) Enum.valueOf(enumType, text.toUpperCase(Locale.ROOT));
        }

        TypeSerializer<T> ser = registry.find(type);
        if (ser != null) {
            return ser.deserialize(raw, this, type);
        }

        if (List.class.isAssignableFrom(type)) {
            return (T) list(raw, Object.class);
        }
        if (Map.class.isAssignableFrom(type)) {
            return (T) map(raw, Object.class, Object.class);
        }

        if (type.isRecord()) {
            return fromRecord(raw, type);
        }
        return fromPojo(raw, type);
    }

    public Object toTree(Object obj) {
        if (obj == null) return null;
        Class<?> rawType = obj.getClass();

        TypeSerializer<Object> ser = (TypeSerializer<Object>) registry.find(rawType);
        if (ser != null) return ser.serialize(obj, this);

        if (isSimpleType(rawType)) return obj;
        if (rawType.isEnum()) return ((Enum<?>) obj).name();

        if (obj instanceof Map<?, ?> map) return mapToTree(map);
        if (obj instanceof Iterable<?> it) return iterableToTree(it);
        if (rawType.isArray()) return arrayToTree(obj);

        if (rawType.isRecord()) return recordToMap(obj);
        return pojoToMap(obj);
    }

    private <T> T fromRecord(Object raw, Class<T> type) {
        if (!(raw instanceof Map<?, ?> map)) throw new IllegalArgumentException("Expected map for " + type.getName());

        ClassIntrospector.ClassMeta meta = ClassIntrospector.get(type);
        List<ClassIntrospector.RecordProperty> props = meta.recordProps();
        Object[] args = new Object[props.size()];

        for (int i = 0; i < props.size(); i++) {
            ClassIntrospector.RecordProperty p = props.get(i);
            Object rawVal = map.get(p.name());
            Class<?> compType = p.rawType();
            Object converted;
            if (List.class.isAssignableFrom(compType)) {
                Class<?> elemType = extractListElemType(p.genericType());
                converted = list(rawVal, elemType);
            } else if (Map.class.isAssignableFrom(compType)) {
                Class<?> keyType = Object.class, valType = Object.class;
                if (p.genericType() instanceof ParameterizedType pt) {
                    Type[] a = pt.getActualTypeArguments();
                    if (a[0] instanceof Class<?> c0) keyType = c0;
                    if (a[1] instanceof Class<?> c1) valType = c1;
                }
                converted = map(rawVal, keyType, valType);
            } else {
                converted = toObject(rawVal, compType);
            }
            args[i] = converted;
        }

        try {
            return (T) meta.ctor().newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T fromPojo(Object raw, Class<T> type) {
        if (!(raw instanceof Map<?, ?> map)) throw new IllegalArgumentException("Expected map for " + type.getName());

        ClassIntrospector.ClassMeta meta = ClassIntrospector.get(type);
        if (meta.ctor() == null) {
            throw new RuntimeException("No no-arg constructor for " + type.getName());
        }
        final T instance;
        try {
            instance = (T) meta.ctor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        for (ClassIntrospector.PojoField f : meta.pojoFields()) {
            Object val = toObject(map.get(f.name()), f.rawType());
            if (f.handle() != null) {
                f.handle().set(instance, val);
            } else {
                try {
                    f.reflectField().set(instance, val);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return instance;
    }

    private Object recordToMap(Object obj) {
        ClassIntrospector.ClassMeta meta = ClassIntrospector.get(obj.getClass());
        Map<String, Object> out = new LinkedHashMap<>();
        for (ClassIntrospector.RecordProperty p : meta.recordProps()) {
            try {
                Object val = p.getter().invoke(obj);
                out.put(p.name(), toTree(val));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return out;
    }

    private Object pojoToMap(Object obj) {
        ClassIntrospector.ClassMeta meta = ClassIntrospector.get(obj.getClass());
        Map<String, Object> out = new LinkedHashMap<>();
        for (ClassIntrospector.PojoField f : meta.pojoFields()) {
            Object v;
            if (f.handle() != null) {
                v = f.handle().get(obj);
            } else {
                try {
                    v = f.reflectField().get(obj);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            out.put(f.name(), toTree(v));
        }
        return out;
    }

    public <T> List<T> list(Object raw, Class<T> elementType) {
        if (raw == null) return List.of();
        if (!(raw instanceof List<?> rawList)) throw new IllegalArgumentException("Expected list");
        List<T> out = new ArrayList<>(rawList.size());
        for (Object o : rawList) {
            out.add(toObject(o, elementType));
        }
        return out;
    }

    public <K, V> Map<K, V> map(Object raw, Class<K> keyType, Class<V> valType) {
        if (raw == null) return Map.of();
        if (!(raw instanceof Map<?, ?> rawMap)) throw new IllegalArgumentException("Expected map");
        Map<K, V> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : rawMap.entrySet()) {
            K k = toObject(e.getKey(), keyType);
            V v = toObject(e.getValue(), valType);
            out.put(k, v);
        }
        return out;
    }

    private Object iterableToTree(Iterable<?> iterable) {
        List<Object> out = new ArrayList<>();
        for (Object element : iterable) {
            out.add(toTree(element));
        }
        return out;
    }

    private Object mapToTree(Map<?, ?> map) {
        Map<Object, Object> out = new LinkedHashMap<>(map.size());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            out.put(entry.getKey(), toTree(entry.getValue()));
        }
        return out;
    }

    private Object arrayToTree(Object array) {
        int length = java.lang.reflect.Array.getLength(array);
        List<Object> out = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            out.add(toTree(java.lang.reflect.Array.get(array, i)));
        }
        return out;
    }

    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || Number.class.isAssignableFrom(type)
                || Boolean.class.equals(type)
                || Character.class.equals(type)
                || String.class.equals(type);
    }

    private Class<?> extractListElemType(Type t) {
        if (t instanceof ParameterizedType pt) {
            Type arg = pt.getActualTypeArguments()[0];
            if (arg instanceof Class<?> c) return c;
        }
        return Object.class;
    }
}
