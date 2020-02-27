package xyz.acrylicstyle.sql.options;

import util.StringCollection;

public interface InsertOptions {
    default StringCollection<Object> getValues() { return null; }

    class Builder {
        private StringCollection<Object> values;

        public Builder() {
            this.values = new StringCollection<>();
        }

        public InsertOptions.Builder addValue(String key, Object value) {
            values.put(key, value);
            return this;
        }

        public InsertOptions build() {
            return new InsertOptions() {
                @Override
                public StringCollection<Object> getValues() {
                    return Builder.this.values;
                }
            };
        }
    }
}

