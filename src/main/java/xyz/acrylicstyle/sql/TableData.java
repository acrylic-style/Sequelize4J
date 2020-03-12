package xyz.acrylicstyle.sql;

import org.jetbrains.annotations.NotNull;
import util.CollectionList;
import util.StringCollection;
import util.promise.Promise;
import xyz.acrylicstyle.sql.options.FindOptions;
import xyz.acrylicstyle.sql.options.IncrementOptions;
import xyz.acrylicstyle.sql.options.InsertOptions;
import xyz.acrylicstyle.sql.options.UpsertOptions;

import java.sql.Connection;
import java.sql.SQLException;

public class TableData implements ITable {
    private Table table;
    private Connection connection;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private StringCollection<TableDefinition> definitions;
    private StringCollection<Object> values;

    public TableData(Table table, Connection connection, StringCollection<TableDefinition> definitions, StringCollection<Object> values) {
        this.table = table;
        this.connection = connection;
        this.definitions = definitions;
        this.values = values;
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
    public Promise<CollectionList<TableData>> update(@NotNull String field, @NotNull UpsertOptions options) {
        return table.update(field, options);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Promise<CollectionList<TableData>> upsert(String field, UpsertOptions options) {
        return table.update(field, options);
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
     * @throws ClassCastException When couldn't cast to clazz
     * @return Value casted to the T
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String field, Class<T> clazz) throws ClassCastException {
        return (T) values.get(field);
    }

    @Override
    public String toString() {
        return "TableData{name='" + getName() + "}";
    }
}
