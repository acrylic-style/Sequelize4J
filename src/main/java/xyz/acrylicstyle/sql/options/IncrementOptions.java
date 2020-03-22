package xyz.acrylicstyle.sql.options;

import xyz.acrylicstyle.sql.Validate;

import java.util.HashMap;
import java.util.Map;

public interface IncrementOptions extends FindOptions {
    default HashMap<String, Integer> getFieldsMap() { return null; }

    class Builder {
        Map<String, Object> where = new HashMap<>();

        HashMap<String, Integer> fieldsMap;

        String orderBy = null;

        Sort order = Sort.ASC;

        public Builder() {
            this.fieldsMap = new HashMap<>();
        }

        public Builder addWhere(String key, Object value) {
            where.put(key, value);
            return this;
        }

        public IncrementOptions.Builder addField(String field, Integer by) {
            this.fieldsMap.put(field, by);
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

        public IncrementOptions build() {
            return new IncrementOptions() {
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

                @Override
                public HashMap<String, Integer> getFieldsMap() {
                    return Builder.this.fieldsMap;
                }
            };
        }
    }
}
