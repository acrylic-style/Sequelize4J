package xyz.acrylicstyle.sql.options;

import util.StringCollection;

import java.util.HashMap;
import java.util.Map;

public interface UpsertOptions extends FindOptions, InsertOptions {
    class Builder {
        private Map<String, Object> where;
        private StringCollection<Object> values;

        public Builder() {
            this.where = new HashMap<>();
            this.values = new StringCollection<>();
        }

        public UpsertOptions.Builder addWhere(String key, Object value) {
            where.put(key, value);
            return this;
        }

        public UpsertOptions.Builder addValue(String key, Object value) {
            values.put(key, value);
            return this;
        }

        public UpsertOptions build() {
            return new UpsertOptions() {
                @Override
                public Map<String, Object> where() {
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
