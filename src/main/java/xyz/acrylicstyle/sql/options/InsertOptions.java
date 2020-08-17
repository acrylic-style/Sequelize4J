package xyz.acrylicstyle.sql.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.StringCollection;

public interface InsertOptions {
    @Nullable
    default StringCollection<Object> getValues() { return null; }

    class Builder {
        @Nullable
        private final StringCollection<Object> values;

        public Builder() {
            this(new StringCollection<>());
        }

        public Builder(@Nullable StringCollection<Object> values) {
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
                public StringCollection<Object> getValues() {
                    return Builder.this.values;
                }
            };
        }
    }
}

