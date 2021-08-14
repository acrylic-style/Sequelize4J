package xyz.acrylicstyle.sql.options;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.acrylicstyle.sql.Validate;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public interface UpsertOptions extends FindOptions, InsertOptions {
    class Builder {
        private final Map<String, Map.Entry<Ops, Object>> where = new HashMap<>();
        private final Map<String, Object> values;

        public Builder() {
            this(new HashMap<>());
        }

        public Builder(@Nullable Map<String, Object> values) {
            this.values = values;
        }

        @Contract("_, _ -> this")
        @NotNull
        public Builder addWhere(@NotNull String key, @Nullable Object value) {
            return addWhere(key, Ops.EQUAL, value);
        }

        @Contract("_, _, _ -> this")
        @NotNull
        public Builder addWhere(@NotNull String key, @NotNull Ops op, @Nullable Object value) {
            Validate.notNull(key, "key cannot be null");
            where.put(key, new AbstractMap.SimpleImmutableEntry<>(op, value));
            return this;
        }

        @Contract("_, _ -> this")
        @NotNull
        public Builder addValue(@NotNull String key, @Nullable Object value) {
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
                public @NotNull Map<String, Map.Entry<Ops, Object>> where() {
                    return Builder.this.where;
                }

                @Override
                public Map<String, Object> getValues() {
                    return Builder.this.values;
                }
            };
        }
    }
}
