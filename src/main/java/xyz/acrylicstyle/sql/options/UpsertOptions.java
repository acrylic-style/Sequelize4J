package xyz.acrylicstyle.sql.options;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.StringCollection;
import xyz.acrylicstyle.sql.Validate;

import java.util.HashMap;
import java.util.Map;

public interface UpsertOptions extends FindOptions, InsertOptions {
    class Builder {
        private final Map<String, Object> where;
        private final StringCollection<Object> values;

        public Builder() {
            this(new StringCollection<>());
        }

        public Builder(@Nullable StringCollection<Object> values) {
            this.where = new HashMap<>();
            this.values = values;
        }

        @Contract("_, _ -> this")
        @NotNull
        public UpsertOptions.Builder addWhere(@NotNull String key, @Nullable Object value) {
            Validate.notNull(key, "key cannot be null");
            where.put(key, value);
            return this;
        }

        @Contract("_, _ -> this")
        @NotNull
        public UpsertOptions.Builder addValue(@NotNull String key, @Nullable Object value) {
            if (values == null) throw new NullPointerException("Cannot add value when values map is null");
            Validate.notNull(key, "key cannot be null");
            values.put(key, value);
            return this;
        }

        @Contract("-> new")
        @NotNull
        public UpsertOptions build() {
            return new UpsertOptions() {
                @Override
                public @NotNull Map<String, Object> where() {
                    return Builder.this.where;
                }

                @Override
                public StringCollection<Object> getValues() {
                    return Builder.this.values;
                }
            };
        }
    }
}
