package com.dractical.femutils.core.data;

import com.dractical.femutils.core.config.ReflectMapper;
import com.dractical.femutils.core.config.TypeRegistry;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class YamlDataEngine implements DataHandle.Engine {
    private final ReflectMapper mapper;
    private final LoaderOptions loaderOptions;
    private final DumperOptions dumperOptions;

    public YamlDataEngine(TypeRegistry registry) {
        this.mapper = new ReflectMapper(Objects.requireNonNull(registry, "registry"));
        this.loaderOptions = safeLoaderOptions();
        this.dumperOptions = prettyDumperOptions();
    }

    @Override
    public <T> T load(DataRef ref, Class<T> type, Supplier<T> defaults) throws IOException {
        Path path = requirePath(ref);
        if (Files.notExists(path)) {
            ensureParentExists(path);
            T def = defaults.get();
            save(ref, def);
            return def;
        }
        String text = Files.readString(path, StandardCharsets.UTF_8);
        Object raw;
        try {
            raw = yaml().load(new StringReader(text));
        } catch (Exception e) {
            throw new IOException("Failed to parse YAML at " + path + ": " + e.getMessage(), e);
        }
        if (raw == null) raw = new LinkedHashMap<>();
        return mapper.toObject(raw, type);
    }

    @Override
    public void save(DataRef ref, Object value) throws IOException {
        Path path = requirePath(ref);
        ensureParentExists(path);
        Object tree = mapper.toTree(value);
        StringWriter out = new StringWriter();
        yaml().dump(tree == null ? Map.of() : tree, out);
        writeAtomically(path, out.toString());
    }

    @Override
    public boolean exists(DataRef ref) {
        return Files.exists(requirePath(ref));
    }

    @Override
    public void delete(DataRef ref) throws IOException {
        Files.deleteIfExists(requirePath(ref));
    }

    @Override
    public void close() {
    }

    private Path requirePath(DataRef ref) {
        if (ref instanceof DataRef.PathRef(Path path)) return path;
        throw new IllegalArgumentException("YAML engine requires a PathRef, got " + ref.getClass().getSimpleName());
    }

    private void ensureParentExists(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
    }

    private void writeAtomically(Path target, String content) throws IOException {
        Path parent = target.toAbsolutePath().normalize().getParent();
        if (parent == null) parent = Path.of(".");
        Path temp = Files.createTempFile(parent, target.getFileName().toString(), ".tmp");
        Files.writeString(temp, content, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
        try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private LoaderOptions safeLoaderOptions() {
        LoaderOptions opts = new LoaderOptions();
        opts.setAllowDuplicateKeys(false);
        opts.setMaxAliasesForCollections(50);
        return opts;
    }

    private DumperOptions prettyDumperOptions() {
        DumperOptions opts = new DumperOptions();
        opts.setIndent(2);
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);
        return opts;
    }

    private Yaml yaml() {
        Representer representer = new Representer(dumperOptions);
        representer.getPropertyUtils().setSkipMissingProperties(true);
        representer.getPropertyUtils().setAllowReadOnlyProperties(false);
        representer.addClassTag(Object.class, Tag.MAP);
        return new Yaml(new SafeConstructor(loaderOptions), representer, dumperOptions, loaderOptions);
    }
}
