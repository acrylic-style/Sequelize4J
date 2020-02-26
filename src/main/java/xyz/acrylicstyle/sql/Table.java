package xyz.acrylicstyle.sql;

import util.CollectionList;
import util.StringCollection;
import xyz.acrylicstyle.sql.options.FindOptions;
import xyz.acrylicstyle.sql.options.UpsertOptions;

import java.sql.*;

public class Table implements ITable {
    private String name;
    private StringCollection<TableDefinition> tableData;
    private Connection connection;

    public Table(String name, StringCollection<TableDefinition> tableData, Connection connection) {
        this.name = name;
        this.tableData = tableData;
        this.connection = connection;
    }

    public TableDefinition getDefinition(String field) { return tableData.get(field); }

    public StringCollection<TableDefinition> getDefinitions() { return tableData; }

    public TableDefinition[] getDefinitionsArray() { return tableData.valuesArray(); }

    public String getName() { return name; }

    public Connection getConnection() { return connection; }

    public TableData set(String field, Object value, FindOptions options) throws SQLException {
        StringBuilder sb = new StringBuilder("update " + this.tableData.get(field).getName() + " set ?=?");
        if (options != null && options.where() != null) {
            sb.append(" where ");
            options.where().forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
        }
        sb.append(";");
        PreparedStatement statement = connection.prepareStatement(sb.toString());
        statement.setString(1, field);
        statement.setObject(2, value);
        statement.executeUpdate();
        return new TableData(this, connection, this.tableData.get(field), value);
    }

    @Override
    public CollectionList<TableData> findAll(String field, FindOptions options) throws SQLException {
        StringBuilder sb = new StringBuilder("select " + field + " from " + this.tableData.get(field).getName());
        if (options != null && options.where() != null) {
            sb.append(" where ");
            options.where().forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
        }
        sb.append(";");
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(sb.toString());
        CollectionList<TableData> tableData = new CollectionList<>();
        while (result.next()) tableData.add(new TableData(this, connection, this.tableData.get(field), result.getObject(field)));
        return tableData;
    }

    @Override
    public TableData findOne(String field, FindOptions options) throws SQLException {
        CollectionList<TableData> list = findAll(field, options);
        return list.size() == 0 ? null : list.first();
    }

    @Override
    public TableData update(String field, Object value, FindOptions options) throws SQLException {
        StringBuilder sb = new StringBuilder("update " + this.tableData.get(field).getName() + " set ?=?");
        if (options != null && options.where() != null) {
            sb.append(" where ");
            options.where().forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
        }
        sb.append(";");
        PreparedStatement statement = connection.prepareStatement(sb.toString());
        statement.setString(1, field);
        statement.setObject(2, value);
        statement.executeUpdate();
        return new TableData(this, connection, this.tableData.get(field), value);
    }

    @Override
    public TableData upsert(String field, Object value, UpsertOptions options) throws SQLException {
        if (findAll(field, options).size() == 0) {
            //
        }
        StringBuilder sb = new StringBuilder("update " + this.tableData.get(field).getName() + " set ?=?");
        if (options != null && options.where() != null) {
            sb.append(" where ");
            options.where().forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
        }
        sb.append(";");
        PreparedStatement statement = connection.prepareStatement(sb.toString());
        statement.setString(1, field);
        statement.setObject(2, value);
        statement.executeUpdate();
        return new TableData(this, connection, this.tableData.get(field), value);
    }

    @Override
    public TableData delete(FindOptions options) throws SQLException {
        return null;
    }

    @Override
    public void drop() throws SQLException {
    }

    @Override
    public String toString() {
        return "Table{name='" + getName() + "',fields=" + tableData.size() + ",connection='" + connection.toString() + "'}";
    }
}
