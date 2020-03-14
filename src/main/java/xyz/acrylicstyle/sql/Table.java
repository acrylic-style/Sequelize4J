package xyz.acrylicstyle.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.CollectionList;
import util.ICollection;
import util.ICollectionList;
import util.StringCollection;
import util.promise.Promise;
import xyz.acrylicstyle.sql.options.FindOptions;
import xyz.acrylicstyle.sql.options.IncrementOptions;
import xyz.acrylicstyle.sql.options.InsertOptions;
import xyz.acrylicstyle.sql.options.UpsertOptions;

import static util.promise.Promise.async;
import static util.promise.Promise.await;

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
    public Promise<CollectionList<TableData>> findAll(@Nullable FindOptions options) {
        return new Promise<CollectionList<TableData>>() {
            @Override
            public CollectionList<TableData> apply(Object o0) {
                try {
                    StringBuilder sb = new StringBuilder("select * from " + getName());
                    CollectionList<Object> values = new CollectionList<>();
                    if (options != null && options.where() != null && options.where().size() != 0) {
                        sb.append(" where ");
                        options.where().forEach((k, v) -> {
                            values.add(v);
                            sb.append(k).append("=?").append(" ");
                        });
                    }
                    sb.append(";");
                    PreparedStatement statement = connection.prepareStatement(sb.toString());
                    values.foreach((o, i) -> {
                        try {
                            statement.setObject(i + 1, o);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    ResultSet result = statement.executeQuery();
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
                        tableData.add(new TableData(Table.this, connection, getDefinitions(), v));
                    }
                    return tableData;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Promise<TableData> findOne(FindOptions options) {
        return async(o0 -> {
            CollectionList<TableData> list = (CollectionList<TableData>) await(findAll(options), null);
            return list.size() == 0 ? null : list.first();
        });
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public Promise<CollectionList<TableData>> update(String field, Object value, FindOptions options) {
        Validate.isTrue(field.matches(Sequelize.FIELD_NAME_REGEX.pattern()), "Field " + field + " must match following pattern: " + Sequelize.FIELD_NAME_REGEX.pattern());
        return async(o1 -> {
            try {
                CollectionList<TableData> dataList = (CollectionList<TableData>) await(findAll(options), null);
                StringBuilder sb = new StringBuilder("update " + getName() + " set " + field + "=?");
                CollectionList<Object> values = new CollectionList<>();
                if (options != null && options.where() != null) {
                    sb.append(" where ");
                    ICollection.asCollection(options.where()).forEach((k, v, i, a) -> {
                        values.add(v);
                        sb.append(k).append("=?");
                        if (i != 0) sb.append(",");
                    });
                }
                sb.append(";");
                PreparedStatement statement = connection.prepareStatement(sb.toString());
                statement.setObject(1, value);
                values.foreach((o, i) -> {
                    try {
                        statement.setObject(i + 2, o);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                statement.executeUpdate();
                return dataList.map(td -> {
                    StringCollection<Object> values2 = td.getValues();
                    values2.add(field, value);
                    td.setValues(values2);
                    return td;
                });
            } catch (SQLException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public Promise<CollectionList<TableData>> update(@NotNull String field, @NotNull UpsertOptions options) {
        Validate.isTrue(field.matches(Sequelize.FIELD_NAME_REGEX.pattern()), "Field " + field + " must match following pattern: " + Sequelize.FIELD_NAME_REGEX.pattern());
        Validate.isTrue(options.getValues() != null && options.getValues().size() != 0, "Values must be specified.");
        return new Promise<CollectionList<TableData>>() {
            @SuppressWarnings("unchecked")
            @Override
            public CollectionList<TableData> apply(Object o0) {
                try {
                    CollectionList<TableData> dataList = (CollectionList<TableData>) await(findAll(options), null);
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
                            statement.setObject(i + 1, o);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    statement.executeUpdate();
                    return dataList.map(td -> {
                        td.setValues(options.getValues());
                        return td;
                    });
                } catch (SQLException e) { throw new RuntimeException(e); }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Promise<CollectionList<TableData>> upsert(String field, UpsertOptions options) {
        Validate.isTrue(field.matches(Sequelize.FIELD_NAME_REGEX.pattern()), "Field " + field + " must match following pattern: " + Sequelize.FIELD_NAME_REGEX.pattern());
        if (((CollectionList<TableData>) await(findAll(options), null)).size() == 0) {
            return async(o -> ICollectionList.ArrayOf((TableData) await(insert(options), null)));
        } else {
            return update(field, options);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Promise<TableData> insert(InsertOptions options) {
        Validate.isTrue(options != null && options.getValues() != null && options.getValues().size() != 0, "InsertOptions must not be null and has 1 key/value at least.");
        return new Promise<TableData>() {
            @Override
            public TableData apply(Object o0) {
                try {
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
                    return new TableData(Table.this, connection, getDefinitions(), options.getValues());
                } catch (SQLException e) { throw new RuntimeException(e); }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Promise<CollectionList<TableData>> delete(FindOptions options) {
        Validate.isTrue(options.where() != null && options.where().size() != 0, "FindOptions(with where clause) must be provided.");
        return new Promise<CollectionList<TableData>>() {
            @SuppressWarnings("unchecked")
            @Override
            public CollectionList<TableData> apply(Object o) {
                try {
                    CollectionList<TableData> dataList = (CollectionList<TableData>) await(findAll(options), null);
                    StringBuilder sb = new StringBuilder("delete from " + getName());
                    if (options.where() != null) {
                        sb.append(" where ");
                        options.where().forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
                    } else throw new IllegalArgumentException("Where clause must be provided.");
                    sb.append(";");
                    Statement statement = connection.createStatement();
                    statement.executeUpdate(sb.toString());
                    return dataList;
                } catch (SQLException e) { throw new RuntimeException(e); }
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Promise<Void> increment(@NotNull IncrementOptions options) {
        Validate.isTrue(options.getFieldsMap() != null && options.getFieldsMap().size() != 0, "IncrementOptions(with fieldsMap) must be provided.");
        return async(o0 -> {
            CollectionList<TableData> data = (CollectionList<TableData>) await(findAll(options), null);
            if (data == null) return null;
            data.forEach(t -> options.getFieldsMap().forEach((k, i) -> t.update(k, t.get(k, Integer.class) + i, options)));
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public Promise<Void> decrement(@NotNull IncrementOptions options) {
        Validate.isTrue(options.getFieldsMap() != null && options.getFieldsMap().size() != 0, "IncrementOptions(with fieldsMap) must be provided.");
        return async(o1 -> {
            CollectionList<TableData> data = (CollectionList<TableData>) await(findAll(options), null);
            if (data == null) return null;
            data.forEach(t -> options.getFieldsMap().forEach((k, i) -> t.update(k, t.get(k, Integer.class) - i, options)));
            return null;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Promise<Void> drop() {
        return async(o1 -> {
            try {
                connection.createStatement().executeUpdate("drop table if exists " + getName());
                return null;
            } catch (SQLException e) { throw new RuntimeException(e); }
        });
    }

    @Override
    public String toString() {
        return "Table{name='" + getName() + "',fields=" + tableData.size() + ",connection='" + connection.toString() + "'}";
    }
}
