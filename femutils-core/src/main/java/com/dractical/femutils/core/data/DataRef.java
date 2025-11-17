package com.dractical.femutils.core.data;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Pointer describing where a payload should be stored.
 */
@SuppressWarnings("unused")
public sealed interface DataRef permits DataRef.PathRef, DataRef.KeyRef {

    String describe();

    static PathRef path(Path path) {
        return new PathRef(path);
    }

    static KeyRef key(Object key) {
        return new KeyRef(key);
    }

    /**
     * Path based reference for file-backed stores.
     */
    record PathRef(Path path) implements DataRef {
        public PathRef {
            Objects.requireNonNull(path, "path");
        }

        @Override
        public String describe() {
            return path.toAbsolutePath().normalize().toString();
        }
    }

    /**
     * Identifier based reference for keyed stores.
     */
    record KeyRef(Object key) implements DataRef {
        public KeyRef {
            Objects.requireNonNull(key, "key");
        }

        @Override
        public String describe() {
            return Objects.toString(key);
        }
    }
}
