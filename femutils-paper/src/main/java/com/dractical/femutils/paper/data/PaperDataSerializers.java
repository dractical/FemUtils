package com.dractical.femutils.paper.data;

import com.dractical.femutils.core.config.ReflectMapper;
import com.dractical.femutils.core.config.TypeRegistry;
import com.dractical.femutils.core.config.TypeSerializer;
import com.dractical.femutils.paper.config.PaperSerializers;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public final class PaperDataSerializers {
    private PaperDataSerializers() {
    }

    public static final TypeSerializer<ItemStack> ITEM_STACK = new TypeSerializer<>() {
        @Override
        public ItemStack deserialize(Object raw, ReflectMapper ctx, Class<ItemStack> type) {
            if (raw == null) return null;
            if (!(raw instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("Expected map for ItemStack");
            }
            Map<String, Object> prepared = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                prepared.put(Objects.toString(e.getKey()), e.getValue());
            }
            return ItemStack.deserialize(prepared);
        }

        @Override
        public Object serialize(ItemStack value, ReflectMapper ctx) {
            return value == null ? null : new LinkedHashMap<>(value.serialize());
        }
    };

    public static final TypeSerializer<Vector> VECTOR = new TypeSerializer<>() {
        @Override
        public Vector deserialize(Object raw, ReflectMapper ctx, Class<Vector> type) {
            if (!(raw instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("Vector must be stored as map");
            }
            return new Vector(num(map, "x"), num(map, "y"), num(map, "z"));
        }

        @Override
        public Object serialize(Vector value, ReflectMapper ctx) {
            return Map.of("x", value.getX(), "y", value.getY(), "z", value.getZ());
        }

        private double num(Map<?, ?> map, String key) {
            Object v = map.get(key);
            if (v instanceof Number n) return n.doubleValue();
            return v != null ? Double.parseDouble(v.toString()) : 0d;
        }
    };

    public static void registerAll(TypeRegistry registry) {
        PaperSerializers.registerAll(registry);
        registry.register(ItemStack.class, ITEM_STACK);
        registry.register(Vector.class, VECTOR);
    }
}
