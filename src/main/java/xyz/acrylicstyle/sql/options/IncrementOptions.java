package xyz.acrylicstyle.sql.options;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.acrylicstyle.sql.Validate;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public interface IncrementOptions extends FindOptions {
    @Nullable
    default HashMap<String, Integer> getFieldsMap() { return null; }

    class Builder {
        @NotNull
        private final Map<String, Map.Entry<Ops, Object>> where = new HashMap<>();

        @NotNull
        private final HashMap<String, Integer> fieldsMap;

        @Nullable
        private String orderBy = null;

        @NotNull
        private Sort order = Sort.ASC;

        public Builder() {
            this.fieldsMap = new HashMap<>();
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
        public IncrementOptions.Builder addField(@NotNull String field, int by) {
            Validate.notNull(field, "field cannot be null");
            this.fieldsMap.put(field, by);
            return this;
        }

        @Contract("_ -> this")
        @NotNull
        public Builder setOrderBy(@Nullable String orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        @Contract("_ -> this")
        @NotNull
        public Builder setOrder(@NotNull Sort order) {
            Validate.notNull(order, "order cannot be null");
            this.order = order;
            return this;
        }

        @Contract("-> new")
        @NotNull
        public IncrementOptions build() {
            return new IncrementOptions() {
                @Override
                public @NotNull Map<String, Map.Entry<Ops, Object>> where() {
                    return Builder.this.where;
                }

                @Override
                public @Nullable String orderBy() {
                    return Builder.this.orderBy;
                }

                @Override
                public @NotNull Sort order() {
                    return Builder.this.order;
                }

                @Override
                public HashMap<String, Integer> getFieldsMap() {
                    return Builder.this.fieldsMap;
                }
            };
        }
    }
}
