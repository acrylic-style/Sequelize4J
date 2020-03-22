package xyz.acrylicstyle.sql.options;

import xyz.acrylicstyle.sql.Validate;

public interface SortOptions {
    default String orderBy() { return null; }

    default Sort order() { return Sort.ASC; }

    class Builder {
        String orderBy = null;

        Sort order = Sort.ASC;

        public Builder setOrderBy(String orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public Builder setOrder(Sort order) {
            Validate.notNull(order, "Order cannot be null");
            this.order = order;
            return this;
        }

        public SortOptions build() {
            return new SortOptions() {
                @Override
                public String orderBy() {
                    return orderBy;
                }

                @Override
                public Sort order() {
                    return order;
                }
            };
        }
    }
}
