package xyz.acrylicstyle.sql;

public abstract class TableDefinition {
    /**
     * @return Name of field. Required.
     */
    public abstract String getName();

    /**
     * @return Data type of this field. Required.
     */
    public abstract DataType<?> getType();

    public final boolean isRequired() { return (getDefaultValue() == null || !allowNull()) && !isAutoIncrement(); }

    /**
     * @return If it's specified as primary key. False by default.
     */
    public boolean isPrimaryKey() { return false; }

    /**
     * @return Does auto increment on create, when data type is int. False by default.
     */
    public boolean isAutoIncrement() { return false; }

    /**
     * @return Default value of this field. Null by default.
     */
    public Object getDefaultValue() { return null; }

    /**
     * @return Does allow null or not. True by default.
     */
    public boolean allowNull() { return true; }

    public static class Builder {
        private final String name;
        private final DataType<?> dataType;
        private boolean primaryKey = false;
        private boolean autoIncrement = false;
        private Object defaultValue = null;
        private boolean allowNull = true;

        public Builder(String name, DataType<?> dataType) {
            Validate.notNull(name, "Name cannot be null");
            Validate.notNull(dataType, "Data type cannot be null");
            this.name = name;
            this.dataType = dataType;
        }

        public Builder setPrimaryKey(boolean b) {
            this.primaryKey = b;
            return this;
        }

        public Builder setAutoIncrement(boolean b) {
            this.autoIncrement = b;
            return this;
        }

        public Builder setDefaultValue(Object o) {
            this.defaultValue = o;
            return this;
        }

        public Builder setAllowNull(boolean b) {
            this.allowNull = b;
            return this;
        }

        public TableDefinition build() {
            return new TableDefinition() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public DataType<?> getType() {
                    return dataType;
                }

                @Override
                public boolean isAutoIncrement() {
                    return autoIncrement;
                }

                @Override
                public boolean allowNull() {
                    return allowNull;
                }

                @Override
                public boolean isPrimaryKey() {
                    return primaryKey;
                }

                @Override
                public Object getDefaultValue() {
                    return defaultValue;
                }
            };
        }
    }
}
