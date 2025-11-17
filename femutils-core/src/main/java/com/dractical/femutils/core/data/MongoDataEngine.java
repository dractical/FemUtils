package com.dractical.femutils.core.data;

import com.dractical.femutils.core.config.ReflectMapper;
import com.dractical.femutils.core.config.TypeRegistry;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class MongoDataEngine implements DataHandle.Engine {
    private final MongoClient client;
    private final MongoCollection<Document> collection;
    private final ReflectMapper mapper;
    private final boolean closeClient;

    public MongoDataEngine(MongoClient client, String database, String collection, TypeRegistry registry) {
        this(client, database, collection, registry, false);
    }

    public MongoDataEngine(MongoClient client, String database, String collection, TypeRegistry registry, boolean closeClient) {
        this.client = Objects.requireNonNull(client, "client");
        Objects.requireNonNull(database, "database");
        Objects.requireNonNull(collection, "collection");
        MongoDatabase db = client.getDatabase(database);
        this.collection = db.getCollection(collection);
        this.mapper = new ReflectMapper(Objects.requireNonNull(registry, "registry"));
        this.closeClient = closeClient;
    }

    @Override
    public <T> T load(DataRef ref, Class<T> type, Supplier<T> defaults) {
        Object key = requireKey(ref);
        Document doc = collection.find(Filters.eq("_id", key)).first();
        if (doc == null) {
            T def = defaults.get();
            save(ref, def);
            return def;
        }
        Map<String, Object> copy = new LinkedHashMap<>(doc);
        copy.remove("_id");
        Object body = (copy.size() == 1 && copy.containsKey("value")) ? copy.get("value") : copy;
        return mapper.toObject(body, type);
    }

    @Override
    public void save(DataRef ref, Object value) {
        Object key = requireKey(ref);
        Object tree = mapper.toTree(value);
        //noinspection unchecked
        Map<String, Object> map = (tree instanceof Map<?, ?> m)
                ? new LinkedHashMap<>((Map<String, Object>) m)
                : new LinkedHashMap<>(Map.of("value", tree));
        map.put("_id", key);
        Document doc = new Document(map);
        collection.replaceOne(Filters.eq("_id", key), doc, new ReplaceOptions().upsert(true));
    }

    @Override
    public boolean exists(DataRef ref) {
        Object key = requireKey(ref);
        return collection.find(Filters.eq("_id", key)).limit(1).first() != null;
    }

    @Override
    public void delete(DataRef ref) {
        Object key = requireKey(ref);
        collection.deleteOne(Filters.eq("_id", key));
    }

    @Override
    public void close() throws IOException {
        if (closeClient) {
            try {
                client.close();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    private Object requireKey(DataRef ref) {
        if (ref instanceof DataRef.KeyRef(Object key)) return key;
        throw new IllegalArgumentException("Mongo engine requires a KeyRef, got " + ref.getClass().getSimpleName());
    }
}
