package xyz.acrylicstyle.sql;

import org.jetbrains.annotations.NotNull;
import util.CollectionList;
import util.StringCollection;
import util.promise.Promise;
import xyz.acrylicstyle.sql.exceptions.IncompatibleTypeException;
import xyz.acrylicstyle.sql.options.FindOptions;
import xyz.acrylicstyle.sql.options.IncrementOptions;
import xyz.acrylicstyle.sql.options.InsertOptions;
import xyz.acrylicstyle.sql.options.UpsertOptions;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class TableData implements ITable {
    private final Table table;
    private final Connection connection;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final StringCollection<TableDefinition> definitions;
    private StringCollection<Object> values;
    private final String statement;

    public TableData(Table table, Connection connection, StringCollection<TableDefinition> definitions, StringCollection<Object> values, String statement) {
        this.table = table;
        this.connection = connection;
        this.definitions = definitions;
        this.values = values;
        this.statement = statement;
    }

    public String getStatement() {
        return statement;
    }

    @Override
    public Connection getConnection() { return connection; }

    @Override
    public String getName() { return table.getName(); }

    public StringCollection<Object> getValues() { return values; }

    void setValues(StringCollection<Object> o) { this.values = o; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Promise<CollectionList<TableData>> findAll(FindOptions options) {
        return table.findAll(options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Promise<TableData> findOne(FindOptions options) {
        return table.findOne(options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Promise<CollectionList<TableData>> update(String field, Object value, FindOptions options) {
        return table.update(field, value, options);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Promise<CollectionList<TableData>> update(@NotNull UpsertOptions options) {
        return table.update(options);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Promise<CollectionList<TableData>> upsert(UpsertOptions options) {
        return table.update(options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Promise<TableData> insert(InsertOptions options) {
        return table.insert(options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Promise<Void> drop() {
        return table.drop();
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Promise<CollectionList<TableData>> delete(@NotNull FindOptions options) {
        return table.delete(options);
    }

    @Override
    public Promise<Void> increment(@NotNull IncrementOptions options) {
        return table.increment(options);
    }

    @Override
    public Promise<Void> decrement(@NotNull IncrementOptions options) {
        return table.decrement(options);
    }

    /**
     * Get field's value.
     * @param field Name of field
     * @return Value.
     */
    public Object get(String field) {
        return values.get(field);
    }

    /**
     * Get field's value.
     * @param field Name of field
     * @throws IncompatibleTypeException When couldn't cast to clazz
     * @return Value casted to the T
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String field, Class<T> clazz) throws IncompatibleTypeException {
        Object value = values.get(field);
        if (value == null) return null;
        TableDefinition def = definitions.get(field);
        Class<?> type = toClass(def.getType());
        if (type.equals(Boolean.class) && value.getClass().equals(Integer.class)) value = ((int) value) != 0;
        if (type.equals(clazz) || type.getCanonicalName().equals(clazz.getCanonicalName())) return (T) value;
        throw new IncompatibleTypeException(def.getType(), type, value.getClass());
    }

    @NotNull
    private Class<?> toClass(@NotNull DataType type) {
        switch (type) {
            case TINYINT:
                return Byte.class;
            case SMALLINT:
                return Short.class;
            case INTEGER:
            case INT:
            case MEDIUMINT:
                return Integer.class;
            case BIGINT:
                return Long.class;
            case FLOAT:
                return Float.class;
            case DECIMAL:
                return BigDecimal.class;
            case CHAR:
            case STRING:
            case TEXT:
                return String.class;
            case BOOL:
            case BOOLEAN:
            case BIT:
                return Boolean.class;
            case BINARY:
            case VARBINARY:
                return Byte[].class;
            case DATETIME:
            case TIMESTAMP:
                return Timestamp.class;
            case DATE:
                return Date.class;
            case TIME:
                return Time.class;
            case ENUM:
                return Enum.class;
            case DOUBLE:
                return Double.class;
            default:
                return Object.class;
        }
    }

    public Integer getInteger(String field) throws IncompatibleTypeException {
        return get(field, Integer.class);
    }

    public String getString(String field) throws IncompatibleTypeException {
        return get(field, String.class);
    }

    public Boolean getBoolean(String field) throws IncompatibleTypeException {
        return get(field, Boolean.class);
    }

    public Enum<?> getEnum(String field) throws IncompatibleTypeException {
        return get(field, Enum.class);
    }

    public Double getDouble(String field) throws IncompatibleTypeException {
        return get(field, Double.class);
    }

    public Time getTime(String field) throws IncompatibleTypeException {
        return get(field, Time.class);
    }

    public Date getDate(String field) throws IncompatibleTypeException {
        return get(field, Date.class);
    }

    public Timestamp getTimestamp(String field) throws IncompatibleTypeException {
        return get(field, Timestamp.class);
    }

    public Byte[] getBinary(String field) throws IncompatibleTypeException {
        return get(field, Byte[].class);
    }

    public BigDecimal getBigDecimal(String field) throws IncompatibleTypeException {
        return get(field, BigDecimal.class);
    }

    public Float getFloat(String field) throws IncompatibleTypeException {
        return get(field, Float.class);
    }

    public Long getLong(String field) throws IncompatibleTypeException {
        return get(field, Long.class);
    }

    public Byte getByte(String field) throws IncompatibleTypeException {
        return get(field, Byte.class);
    }

    public Short getShort(String field) throws IncompatibleTypeException {
        return get(field, Short.class);
    }

    @Override
    public String toString() {
        return "TableData{name='" + getName() + "}";
    }
}
