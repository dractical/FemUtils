package com.dractical.femutils.paper.config;

import com.dractical.femutils.core.config.*;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public final class SimpleYamlEngine implements ConfigHandle.Engine {
    private final ReflectMapper mapper;
    private final TypeRegistry registry;

    public SimpleYamlEngine(TypeRegistry registry) {
        this.registry = Objects.requireNonNull(registry);
        this.mapper = new ReflectMapper(registry);
    }

    @Override
    public <T> T load(Path path, Class<T> type, Supplier<T> defaults) throws IOException {
        YamlFile yaml = new YamlFile(path.toFile());
        if (Files.notExists(path)) {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            yaml.createNewFile(true);
            T def = defaults.get();
            writeObject(yaml, "", def, type);
            yaml.save();
            return def;
        }
        try {
            yaml.load();
        } catch (Exception e) {
            throw new IOException("Failed to load YAML " + path + ": " + e.getMessage(), e);
        }
        ConfigurationSection section = yaml.getConfigurationSection("");
        Object tree = section != null ? section.getMapValues(false) : new LinkedHashMap<>();
        return mapper.toObject(tree, type);
    }

    @Override
    public void save(Path path, Object value) throws IOException {
        YamlFile yaml = new YamlFile(path.toFile());
        try {
            yaml.load();
        } catch (Exception ignored) {
        }
        writeObject(yaml, "", value, value.getClass());
        yaml.save();
    }

    private void writeObject(YamlFile yaml, String basePath, Object obj, Class<?> type) {
        if (obj == null) return;

        if (basePath.isEmpty()) {
            Header headerAnnotation = type.getAnnotation(Header.class);
            if (headerAnnotation != null) {
                yaml.setHeader(String.join("\n", headerAnnotation.value()));
            }
        }

        if (registry.find(type) != null) {
            yaml.set(basePath, mapper.toTree(obj));
            return;
        }

        if (obj instanceof Iterable<?> iterable) {
            yaml.set(basePath, mapper.toTree(iterable));
            return;
        }
        if (obj instanceof Map<?, ?> map) {
            yaml.set(basePath, mapper.toTree(map));
            return;
        }
        if (type.isArray()) {
            int length = java.lang.reflect.Array.getLength(obj);
            List<Object> values = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                values.add(mapper.toTree(java.lang.reflect.Array.get(obj, i)));
            }
            yaml.set(basePath, values);
            return;
        }
        if (isLeafType(type)) {
            if (obj instanceof Enum<?> enumVal) {
                yaml.set(basePath, enumVal.name());
                return;
            }
            yaml.set(basePath, obj);
            return;
        }

        ClassIntrospector.ClassMeta meta = ClassIntrospector.get(type);
        if (meta.isRecord()) {
            for (ClassIntrospector.RecordProperty rc : meta.recordProps()) {
                Object val;
                try {
                    val = rc.getter().invoke(obj);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                String path = concat(basePath, rc.name());
                if (rc.comment() != null) {
                    yaml.setComment(path, String.join("\n", rc.comment().value()));
                }
                writeObject(yaml, path, val, rc.rawType());
            }
            return;
        }

        for (ClassIntrospector.PojoField f : meta.pojoFields()) {
            Object val;
            if (f.handle() != null) {
                val = f.handle().get(obj);
            } else {
                try {
                    val = f.reflectField().get(obj);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            String path = concat(basePath, f.name());
            if (f.comment() != null) {
                yaml.setComment(path, String.join("\n", f.comment().value()));
            }
            writeObject(yaml, path, val, f.rawType());
        }
    }

    private boolean isLeafType(Class<?> type) {
        return type.isPrimitive()
                || String.class.equals(type)
                || Number.class.isAssignableFrom(type)
                || Boolean.class.equals(type)
                || Character.class.equals(type)
                || type.isEnum();
    }

    private String concat(String base, String child) {
        return base.isEmpty() ? child : base + "." + child;
    }
}
