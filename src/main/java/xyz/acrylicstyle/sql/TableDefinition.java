package xyz.acrylicstyle.sql;

public abstract class TableDefinition {
    /**
     * @return Name of field. Required.
     */
    public abstract String getName();

    /**
     * @return Data type of this field. Required.
     */
    public abstract DataType getType();

    public boolean isRequired() { return (getDefaultValue() == null || !allowNull()) && !isAutoIncrement(); }

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
}
