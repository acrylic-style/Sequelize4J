package xyz.acrylicstyle.sql.options;

import util.ICollectionList;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IncrementOptions extends FindOptions {
    private String field = null;
    private String[] fields = null;
    private HashMap<String, Integer> fieldsMap = null;

    public String getField() { return field; }

    public String[] getFields() { return fields; }

    public HashMap<String, Integer> getFieldsMap() { return fieldsMap; }

    public static class Builder {
        private Map<String, Object> where;
        private HashMap<String, Integer> fieldsMap;
        private String[] fields;
        private String field;

        public Builder() {
            this.where = new HashMap<>();
            this.fieldsMap = new HashMap<>();
            this.fields = new String[] {};
            this.field = null;
        }

        public IncrementOptions.Builder addWhere(String key, Object value) {
            where.put(key, value);
            return this;
        }

        public IncrementOptions.Builder setWhere(String field) {
            this.field = field;
            return this;
        }

        public IncrementOptions.Builder addField(String field) {
            this.fields = ICollectionList.asList(this.fields).addAll(ICollectionList.asList(Collections.singletonList(field))).toArray(new String[0]);
            return this;
        }

        public IncrementOptions.Builder setFields(String[] fields) {
            this.fields = fields;
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
                public String getField() {
                    return Builder.this.field;
                }

                @Override
                public String[] getFields() {
                    return Builder.this.fields;
                }

                @Override
                public HashMap<String, Integer> getFieldsMap() {
                    return Builder.this.fieldsMap;
                }
            };
        }
    }
}
