package xyz.acrylicstyle.sql;

import org.jetbrains.annotations.NotNull;
import util.CollectionList;
import util.StringCollection;
import util.promise.rewrite.Promise;
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
    private final StringCollection<TableDefinition> definitions;
    private StringCollection<Object> values;
    private final String statement;

    public TableData(Table table, Connection connection, StringCollection<TableDefinition> definitions, StringCollection<Object> values, String statement) {
        this.table = table;
        this.connection = connection;
        this.definitions = definitions;
        this.values = values == null ? new StringCollection<>() : values;
        this.statement = statement;
    }

    /**
     * Returns the all table definitions
     * @return table definitions
     */
    @NotNull
    public StringCollection<TableDefinition> getDefinitions() {
        return definitions;
    }

    /**
     * @return the table
     */
    @NotNull
    public Table getTable() {
        return table;
    }

    /**
     * @return the statement which was used to obtain this TableData.
     */
    @NotNull
    public String getStatement() {
        return statement;
    }

    /**
     * @return the connection that was used to execute statement
     */
    @NotNull
    @Override
    public Connection getConnection() { return connection; }

    /**
     * @return name of the table
     */
    @NotNull
    @Override
    public String getName() { return table.getName(); }

    /**
     * @return values (query result)
     */
    @NotNull
    public StringCollection<Object> getValues() { return values; }

    /**
     * An internal method used to update existing table data.
     * @param o new values
     */
    void setValues(StringCollection<Object> o) { this.values = o == null ? new StringCollection<>() : o; }

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Promise<Void> increment(@NotNull IncrementOptions options) {
        return table.increment(options);
    }

    /**
     * {@inheritDoc}
     */
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
        Class<?> type = def.getType().toClass();
        if (type.equals(Boolean.class) && value.getClass().equals(Integer.class)) value = ((int) value) != 0;
        if (type.equals(clazz) || type.getCanonicalName().equals(clazz.getCanonicalName())) return (T) value;
        throw new IncompatibleTypeException(def.getType(), type, value.getClass());
    }

    /**
     * Get field's value.
     * @param field Name of field
     * @throws IncompatibleTypeException When couldn't cast to clazz
     * @throws IllegalArgumentException if the DataType is special type
     * @return Value casted to the T
     */
    public <T> T get(String field, DataType<T> type) throws IncompatibleTypeException {
        if (type.isSpecialType()) throw new IllegalArgumentException("Cannot get special type");
        return get(field, type.toClass());
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
        return "TableData{table='" + table.getName() + "', values=" + values + "}";
    }
}
