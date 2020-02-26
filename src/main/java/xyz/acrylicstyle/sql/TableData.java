package xyz.acrylicstyle.sql;

import util.CollectionList;
import xyz.acrylicstyle.sql.options.FindOptions;
import xyz.acrylicstyle.sql.options.IncrementOptions;
import xyz.acrylicstyle.sql.options.UpsertOptions;

import java.sql.*;
import java.util.Collections;
import java.util.Map;

public class TableData implements ITable {
    private Table table;
    private Connection connection;
    private TableDefinition definition;
    private String fieldName;
    private Object value;

    public TableData(Table table, Connection connection, TableDefinition definition, Object value) {
        this.table = table;
        this.connection = connection;
        this.definition = definition;
        this.fieldName = definition.getName();
        this.value = value;
    }

    public TableDefinition getDefinition() { return definition; }

    public Object getDefaultValue() { return definition.getDefaultValue(); }

    @Override
    public Connection getConnection() { return connection; }

    @Override
    public String getName() { return fieldName; }

    public Object getValue() { return value; }

    @Override
    public TableData set(String field, Object value, FindOptions options) throws SQLException {
        StringBuilder sb = new StringBuilder("update " + table.getName() + " set ?=?");
        if (options != null && options.where() != null) {
            sb.append(" where ");
            options.where().forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
        }
        sb.append(";");
        PreparedStatement statement = connection.prepareStatement(sb.toString());
        statement.setString(1, field);
        statement.setObject(2, value);
        statement.executeUpdate();
        return this;
    }

    public TableData set(Object value) throws SQLException {
        set(getName(), value, new FindOptions.Builder().addWhere(getName(), TableData.this.value).build());
        return new TableData(table, connection, definition, value);
    }

    @Override
    public CollectionList<TableData> findAll(String field, FindOptions options) throws SQLException {
        StringBuilder sb = new StringBuilder("select " + field + " from " + table.getName());
        if (options != null && options.where() != null) {
            sb.append(" where ");
            options.where().forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
        }
        sb.append(";");
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(sb.toString());
        CollectionList<TableData> tableData = new CollectionList<>();
        while (result.next()) tableData.add(new TableData(table, connection, definition, result.getObject(field)));
        return tableData;
    }

    @Override
    public TableData findOne(String field, FindOptions options) throws SQLException {
        CollectionList<TableData> list = findAll(field, options);
        return list.size() == 0 ? null : list.first();
    }

    @Override
    public TableData update(String field, Object value, FindOptions options) throws SQLException {
        StringBuilder sb = new StringBuilder("update " + getName() + " set ?=?");
        if (options != null && options.where() != null) {
            sb.append(" where ");
            options.where().forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
        }
        sb.append(";");
        PreparedStatement statement = connection.prepareStatement(sb.toString());
        statement.setString(1, field);
        statement.setObject(2, value);
        statement.executeUpdate();
        return new TableData(table, connection, definition, value);
    }

    @Override
    public TableData upsert(String field, Object value, UpsertOptions options) throws SQLException {
        return null;
    }

    @Override
    public void drop() throws SQLException {

    }

    @Override
    public TableData delete(FindOptions options) throws SQLException {
        return null;
    }

    @Override
    public String toString() {
        return "TableData{name='" + getName() + "',allowNull=" + definition.allowNull() + ",defaultValue=" + getDefaultValue() + "}";
    }
}
