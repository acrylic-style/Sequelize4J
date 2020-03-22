package xyz.acrylicstyle.sql.options;

import xyz.acrylicstyle.sql.Validate;

import java.util.HashMap;
import java.util.Map;

public interface FindOptions extends SortOptions {
    default Map<String, Object> where() { return null; }

    class Builder {
        Map<String, Object> where;

        String orderBy = null;

        Sort order = Sort.ASC;

        public Builder() {
            this.where = new HashMap<>();
        }

        public Builder addWhere(String key, Object value) {
            where.put(key, value);
            return this;
        }

        public Builder setOrderBy(String orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public Builder setOrder(Sort order) {
            Validate.notNull(order, "Order cannot be null");
            this.order = order;
            return this;
        }

        public FindOptions build() {
            return new FindOptions() {
                @Override
                public Map<String, Object> where() {
                    return Builder.this.where;
                }

                @Override
                public String orderBy() {
                    return Builder.this.orderBy;
                }

                @Override
                public Sort order() {
                    return Builder.this.order;
                }
            };
        }
    }
}
