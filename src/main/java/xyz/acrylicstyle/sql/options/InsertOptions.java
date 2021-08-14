package xyz.acrylicstyle.sql.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public interface InsertOptions {
    @Nullable
    default Map<String, Object> getValues() { return null; }

    class Builder {
        @Nullable
        private final Map<String, Object> values;

        public Builder() {
            this(new HashMap<>());
        }

        public Builder(@Nullable Map<String, Object> values) {
            this.values = values;
        }

        public InsertOptions.Builder addValue(String key, Object value) {
            if (values == null) throw new NullPointerException("Cannot add value when values map is null");
            values.put(key, value);
            return this;
        }

        @NotNull
        public InsertOptions build() {
            return new InsertOptions() {
                @Override
                public Map<String, Object> getValues() {
                    return Builder.this.values;
                }
            };
        }
    }
}

