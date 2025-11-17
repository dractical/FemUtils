package com.dractical.femutils.core.data;

import com.dractical.femutils.core.config.ReflectMapper;
import com.dractical.femutils.core.config.TypeRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class MySqlDataEngine implements DataHandle.Engine {
    private final Supplier<Connection> connectionSupplier;
    private final AutoCloseable closeable;
    private final ReflectMapper mapper;
    private final ObjectMapper json;
    private final String table;
    private final String idColumn;
    private final String payloadColumn;
    private final boolean autoCreateTable;
    private final AtomicBoolean tableReady = new AtomicBoolean(false);

    public MySqlDataEngine(DataSource dataSource, String table, String idColumn, String payloadColumn, TypeRegistry registry, boolean autoCreateTable) {
        this(() -> getConnection(dataSource), dataSource instanceof AutoCloseable ac ? ac : null, table, idColumn, payloadColumn, registry, autoCreateTable);
    }

    public MySqlDataEngine(Supplier<Connection> connectionSupplier, String table, String idColumn, String payloadColumn, TypeRegistry registry, boolean autoCreateTable) {
        this(connectionSupplier, null, table, idColumn, payloadColumn, registry, autoCreateTable);
    }

    private MySqlDataEngine(Supplier<Connection> connectionSupplier, AutoCloseable closeable, String table, String idColumn, String payloadColumn, TypeRegistry registry, boolean autoCreateTable) {
        this.connectionSupplier = Objects.requireNonNull(connectionSupplier, "connectionSupplier");
        this.closeable = closeable;
        this.table = sanitizeName(table, "table");
        this.idColumn = sanitizeName(idColumn, "idColumn");
        this.payloadColumn = sanitizeName(payloadColumn, "payloadColumn");
        this.mapper = new ReflectMapper(Objects.requireNonNull(registry, "registry"));
        this.json = new ObjectMapper();
        this.autoCreateTable = autoCreateTable;
    }

    @Override
    public <T> T load(DataRef ref, Class<T> type, Supplier<T> defaults) throws IOException {
        ensureTable();
        Object key = requireKey(ref);

        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement("SELECT `" + payloadColumn + "` FROM `" + table + "` WHERE `" + idColumn + "`=? LIMIT 1")) {
            ps.setObject(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    T def = defaults.get();
                    save(ref, def);
                    return def;
                }
                String payload = rs.getString(1);
                Object raw = payload == null ? null : json.readValue(payload, Object.class);
                return mapper.toObject(raw, type);
            }
        } catch (SQLException e) {
            throw new IOException("Failed to load from MySQL: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(DataRef ref, Object value) throws IOException {
        ensureTable();
        Object key = requireKey(ref);
        String payload;
        try {
            payload = json.writeValueAsString(mapper.toTree(value));
        } catch (JsonProcessingException e) {
            throw new IOException("Unable to encode value as JSON", e);
        }

        String sql = "INSERT INTO `" + table + "`(`" + idColumn + "`, `" + payloadColumn + "`) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE `" + payloadColumn + "`=VALUES(`" + payloadColumn + "`)";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, key);
            ps.setString(2, payload);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IOException("Failed to save to MySQL: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(DataRef ref) throws IOException {
        ensureTable();
        Object key = requireKey(ref);
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM `" + table + "` WHERE `" + idColumn + "`=? LIMIT 1")) {
            ps.setObject(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IOException("Failed to check existence: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(DataRef ref) throws IOException {
        ensureTable();
        Object key = requireKey(ref);
        try (Connection c = connection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM `" + table + "` WHERE `" + idColumn + "`=?")) {
            ps.setObject(1, key);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IOException("Failed to delete row: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                if (e instanceof IOException io) throw io;
                throw new IOException(e);
            }
        }
    }

    private void ensureTable() throws IOException {
        if (!autoCreateTable || tableReady.get()) return;
        synchronized (tableReady) {
            if (tableReady.get()) return;
            String ddl = "CREATE TABLE IF NOT EXISTS `" + table + "` (" +
                    "`" + idColumn + "` VARCHAR(191) NOT NULL PRIMARY KEY," +
                    "`" + payloadColumn + "` LONGTEXT NOT NULL" +
                    ")";
            try (Connection c = connection(); Statement st = c.createStatement()) {
                st.executeUpdate(ddl);
                tableReady.set(true);
            } catch (SQLException e) {
                throw new IOException("Failed creating table " + table + ": " + e.getMessage(), e);
            }
        }
    }

    private Object requireKey(DataRef ref) {
        if (ref instanceof DataRef.KeyRef(Object key)) return key;
        throw new IllegalArgumentException("MySQL engine requires a KeyRef, got " + ref.getClass().getSimpleName());
    }

    private Connection connection() throws SQLException {
        try {
            return connectionSupplier.get();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException se) throw se;
            throw e;
        }
    }

    private static Connection getConnection(DataSource ds) {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private String sanitizeName(String name, String label) {
        Objects.requireNonNull(name, label);
        if (!name.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid " + label + " name: " + name);
        }
        return name;
    }
}
