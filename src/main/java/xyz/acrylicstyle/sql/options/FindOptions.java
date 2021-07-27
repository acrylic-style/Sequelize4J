package xyz.acrylicstyle.sql.options;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.acrylicstyle.sql.Validate;

import java.util.HashMap;
import java.util.Map;

public interface FindOptions extends SortOptions {
    FindOptions.Builder ALL_BUILDER = new FindOptions.Builder().addWhere("true", "true");
    FindOptions ALL = ALL_BUILDER.build();

    @Nullable
    default Map<String, Object> where() { return new HashMap<>(); }

    @Nullable
    default Integer limit() { return null; }

    class Builder {
        @NotNull
        private final Map<String, Object> where;

        @Nullable
        private String orderBy = null;

        @NotNull
        private Sort order = Sort.ASC;

        private Integer limit = null;

        public Builder() {
            this.where = new HashMap<>();
        }

        @Contract("_, _ -> this")
        @NotNull
        public Builder addWhere(@NotNull String key, @Nullable Object value) {
            Validate.notNull(key, "key cannot be null");
            where.put(key, value);
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

        @Contract("_ -> this")
        @NotNull
        public Builder setLimit(@Nullable Integer limit) {
            this.limit = limit;
            return this;
        }

        @Contract("-> new")
        @NotNull
        public FindOptions build() {
            return new FindOptions() {
                @Override
                public @NotNull Map<String, Object> where() {
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
                public Integer limit() {
                    return Builder.this.limit;
                }
            };
        }
    }
}
