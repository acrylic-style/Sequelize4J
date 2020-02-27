package xyz.acrylicstyle.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.CollectionList;
import util.ICollectionList;
import util.StringCollection;
import xyz.acrylicstyle.sql.options.FindOptions;
import xyz.acrylicstyle.sql.options.IncrementOptions;
import xyz.acrylicstyle.sql.options.InsertOptions;
import xyz.acrylicstyle.sql.options.UpsertOptions;
import xyz.acrylicstyle.sql.utils.Validate;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public CollectionList<TableData> findAll(FindOptions options) throws SQLException {
        StringBuilder sb = new StringBuilder("select * from " + getName());
        if (options != null && options.where() != null) {
            sb.append(" where ");
            options.where().forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
        }
        sb.append(";");
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(sb.toString());
        CollectionList<TableData> tableData = new CollectionList<>();
        while (result.next()) {
            StringCollection<Object> v = new StringCollection<>();
            getDefinitions().forEach((k, d) -> {
                try {
                    v.add(k, result.getObject(k));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            tableData.add(new TableData(this, connection, getDefinitions(), v));
        }
        return tableData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableData findOne(FindOptions options) throws SQLException {
        CollectionList<TableData> list = findAll(options);
        return list.size() == 0 ? null : list.first();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public CollectionList<TableData> update(String field, Object value, FindOptions options) throws SQLException {
        Validate.isTrue(field.matches(Sequelize.FIELD_NAME_REGEX.pattern()), "Field " + field + " must match following pattern: " + Sequelize.FIELD_NAME_REGEX.pattern());
        CollectionList<TableData> dataList = findAll(options);
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
        return dataList.map(td -> {
            StringCollection<Object> values = td.getValues();
            values.add(field, value);
            td.setValues(values);
            return td;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CollectionList<TableData> update(@NotNull String field, @NotNull UpsertOptions options) throws SQLException {
        Validate.isTrue(field.matches(Sequelize.FIELD_NAME_REGEX.pattern()), "Field " + field + " must match following pattern: " + Sequelize.FIELD_NAME_REGEX.pattern());
        Validate.isTrue(options.getValues() != null && options.getValues().size() != 0, "Values must be specified.");
        CollectionList<TableData> dataList = findAll(options);
        String columns = options.getValues().keysList().map(s -> s + " = ?").join(", ");
        StringBuilder sb = new StringBuilder("update " + getName() + " set " + columns);
        if (options.where() != null) {
            sb.append(" where ");
            options.where().forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
        }
        sb.append(";");
        PreparedStatement statement = connection.prepareStatement(sb.toString());
        options.getValues().valuesList().foreach((o, i) -> {
            try {
                statement.setObject(i+1, o);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        statement.executeUpdate();
        return dataList.map(td -> {
            td.setValues(options.getValues());
            return td;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CollectionList<TableData> upsert(String field, UpsertOptions options) throws SQLException {
        Validate.isTrue(field.matches(Sequelize.FIELD_NAME_REGEX.pattern()), "Field " + field + " must match following pattern: " + Sequelize.FIELD_NAME_REGEX.pattern());
        if (findAll(options).size() == 0) {
            return ICollectionList.ArrayOf(insert(field, options));
        } else {
            return update(field, options);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableData insert(String field, InsertOptions options) throws SQLException {
        Validate.isTrue(field.matches(Sequelize.FIELD_NAME_REGEX.pattern()), "Field " + field + " must match following pattern: " + Sequelize.FIELD_NAME_REGEX.pattern());
        Validate.isTrue(options != null && options.getValues() != null && options.getValues().size() != 0, "InsertOptions must not be null and has 1 key/value at least.");
        String columns = options.getValues().keysList().join(", ");
        String values = options.getValues().valuesList().map(s -> "?").join(", ");
        PreparedStatement statement = connection.prepareStatement("insert into " + getName() + " (" + columns + ") values (" + values + ")" + ";");
        options.getValues().valuesList().foreach((o, i) -> {
            try {
                statement.setObject(i+1, o);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        statement.executeUpdate();
        return new TableData(this, connection, getDefinitions(), options.getValues());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public CollectionList<TableData> delete(FindOptions options) throws SQLException {
        Validate.isTrue(options.where() != null && options.where().size() != 0, "FindOptions(with where clause) must be provided.");
        CollectionList<TableData> dataList = findAll(options);
        StringBuilder sb = new StringBuilder("delete from " + getName());
        if (options.where() != null) {
            sb.append(" where ");
            options.where().forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
        } else throw new IllegalArgumentException("Where clause must be provided.");
        sb.append(";");
        Statement statement = connection.createStatement();
        statement.executeUpdate(sb.toString());
        return dataList;
    }

    @Override
    public void increment(@NotNull IncrementOptions options) throws SQLException {
        Validate.isTrue(options.getFieldsMap() != null && options.getFieldsMap().size() != 0, "IncrementOptions(with fieldsMap) must be provided.");
        CollectionList<TableData> data = findAll(options);
        if (data == null) return;
        data.forEach(t -> options.getFieldsMap().forEach((k, i) -> {
            try {
                t.update(k, t.get(k, Integer.class) + i, options);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    public void decrement(@NotNull IncrementOptions options) throws SQLException {
        Validate.isTrue(options.getFieldsMap() != null && options.getFieldsMap().size() != 0, "IncrementOptions(with fieldsMap) must be provided.");
        CollectionList<TableData> data = findAll(options);
        if (data == null) return;
        data.forEach(t -> options.getFieldsMap().forEach((k, i) -> {
            try {
                t.update(k, t.get(k, Integer.class) - i, options);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drop() throws SQLException {
        connection.createStatement().executeUpdate("drop table if exists " + getName());
    }

    @Override
    public String toString() {
        return "Table{name='" + getName() + "',fields=" + tableData.size() + ",connection='" + connection.toString() + "'}";
    }
}
