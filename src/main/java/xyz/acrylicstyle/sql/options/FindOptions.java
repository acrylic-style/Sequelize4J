package xyz.acrylicstyle.sql.options;

import java.util.HashMap;
import java.util.Map;

public class FindOptions {
    public Map<String, Object> where() { return null; }

    public static class Builder {
        private Map<String, Object> where;

        public Builder() {
            this.where = new HashMap<>();
        }

        public Builder addWhere(String key, Object value) {
            where.put(key, value);
            return this;
        }

        public FindOptions build() {
            return new FindOptions() {
                @Override
                public Map<String, Object> where() {
                    return Builder.this.where;
                }
            };
        }
    }
}
