package xyz.acrylicstyle.sql.options;

import java.util.HashMap;
import java.util.Map;

public interface IncrementOptions extends FindOptions {
    default HashMap<String, Integer> getFieldsMap() { return null; }

    class Builder {
        private Map<String, Object> where;
        private HashMap<String, Integer> fieldsMap;

        public Builder() {
            this.where = new HashMap<>();
            this.fieldsMap = new HashMap<>();
        }

        public IncrementOptions.Builder addWhere(String key, Object value) {
            where.put(key, value);
            return this;
        }

        public IncrementOptions.Builder addField(String field, Integer by) {
            this.fieldsMap.put(field, by);
            return this;
        }

        public IncrementOptions build() {
            return new IncrementOptions() {
                @Override
                public Map<String, Object> where() {
                    return Builder.this.where;
                }

                @Override
                public HashMap<String, Integer> getFieldsMap() {
                    return Builder.this.fieldsMap;
                }
            };
        }
    }
}
