package xyz.acrylicstyle.sql.options;

import util.Collection;

import java.util.HashMap;
import java.util.Map;

public class UpsertOptions extends FindOptions {
    public Map<String, Object> getDefaults() { return null; }

    public static class Builder {
        private Map<String, Object> where;
        private Collection<String, Object> defaults;

        public Builder() {
            this.where = new HashMap<>();
            this.defaults = new Collection<>();
        }

        public UpsertOptions.Builder addWhere(String key, Object value) {
            where.put(key, value);
            return this;
        }

        public UpsertOptions.Builder addDefault(String key, Object value) {
            defaults.put(key, value);
            return this;
        }

        public UpsertOptions build() {
            return new UpsertOptions() {
                @Override
                public Map<String, Object> where() {
                    return Builder.this.where;
                }

                @Override
                public Collection<String, Object> getDefaults() {
                    return Builder.this.defaults;
                }
            };
        }
    }
}
