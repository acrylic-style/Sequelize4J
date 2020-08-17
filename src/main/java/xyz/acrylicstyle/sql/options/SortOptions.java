package xyz.acrylicstyle.sql.options;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.acrylicstyle.sql.Validate;

public interface SortOptions {
    @Nullable
    default String orderBy() { return null; }

    @NotNull
    default Sort order() { return Sort.ASC; }

    class Builder {
        private String orderBy = null;

        @NotNull
        private Sort order = Sort.ASC;

        @Contract("_ -> this")
        @NotNull
        public Builder setOrderBy(@Nullable String orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        @NotNull
        public Builder setOrder(@NotNull Sort order) {
            Validate.notNull(order, "Order cannot be null");
            this.order = order;
            return this;
        }

        @Contract("-> new")
        @NotNull
        public SortOptions build() {
            return new SortOptions() {
                @Override
                public @NotNull String orderBy() {
                    return orderBy;
                }

                @Override
                public @NotNull Sort order() {
                    return order;
                }
            };
        }
    }
}
