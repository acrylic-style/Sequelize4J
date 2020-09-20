package xyz.acrylicstyle.sql;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static util.promise.Promise.async;
import static util.promise.Promise.await;
import static util.promise.Promise.awaitT;

public class Table implements ITable {
    private final String name;
    private final StringCollection<TableDefinition> tableData;
    private final Connection connection;
    private final Sequelize sequelize;

    public Table(String name, StringCollection<TableDefinition> tableData, Connection connection, Sequelize sequelize) {
        this.name = name;
        this.tableData = tableData;
        this.connection = connection;
        this.sequelize = sequelize;
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
                    if (options != null) {
                        if (options.where() != null && Objects.requireNonNull(options.where()).size() != 0) {
                            sb.append(" where ");
                            Objects.requireNonNull(options.where()).forEach((k, v) -> {
                                values.add(v);
                                sb.append(k).append("=?").append(" ");
                            });
                        }
                        if (options.orderBy() != null && !Objects.equals(options.orderBy(), "")) {
                            sb.append(" order by ").append(options.orderBy()).append(" ").append(options.order().name());
                        }
                        if (options.limit() != null) sb.append(" limit ").append(options.limit());
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
                        tableData.add(new TableData(Table.this, connection, getDefinitions(), v, sb.toString()));
                    }
                    return tableData;
                } catch (CommunicationsException e2) {
                    try {
                        sequelize.authenticate();
                        return findAll(options).complete();
                    } catch (SQLException e3) {
                        throw new RuntimeException(e3);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Promise<TableData> findOne(FindOptions options) {
        findAll(options).then(list -> list.size() == 0 ? null : list.first());
        return async(o0 -> {
            CollectionList<TableData> list = findAll(options).complete();
            assert list != null;
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
                assert dataList != null;
                return dataList.map(td -> {
                    StringCollection<Object> values2 = td.getValues();
                    values2.add(field, value);
                    td.setValues(values2);
                    return td;
                });
            } catch (CommunicationsException e2) {
                try {
                    sequelize.authenticate();
                    return null;
                } catch (SQLException e3) {
                    throw new RuntimeException(e3);
                }
            } catch (SQLException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public @NotNull Promise<@NotNull CollectionList<@NotNull TableData>> update(@NotNull UpsertOptions options) {
        Validate.isTrue(options.getValues() != null && options.getValues().size() != 0, "Values must be specified.");
        return new Promise<CollectionList<TableData>>() {
            @Override
            public CollectionList<TableData> apply(Object o0) {
                try {
                    CollectionList<TableData> dataList = findAll(options).complete();
                    String columns = options.getValues().keysList().map(s -> s + " = ?").join(", ");
                    StringBuilder sb = new StringBuilder("update " + getName() + " set " + columns);
                    @Nullable final Map<String, Object> where = options.where();
                    if (where != null) {
                        sb.append(" where ");
                        sb.append(new CollectionList<>(where.keySet()).map(s -> s + " = ?").join(", ")).append(" ");
                    }
                    sb.append(";");
                    PreparedStatement statement = connection.prepareStatement(sb.toString());
                    AtomicInteger index = new AtomicInteger();
                    options.getValues().valuesList().foreach((o, i) -> {
                        try {
                            statement.setObject(index.incrementAndGet(), o);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    if (where != null) {
                        where.values().forEach(o -> {
                            try {
                                statement.setObject(index.incrementAndGet(), o);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    statement.executeUpdate();
                    assert dataList != null;
                    return dataList.map(td -> {
                        td.setValues(options.getValues());
                        return td;
                    });
                } catch (CommunicationsException e2) {
                    try {
                        sequelize.authenticate();
                    } catch (SQLException e3) {
                        throw new RuntimeException(e3);
                    }
                    return update(options).complete();
                } catch (SQLException e) { throw new RuntimeException(e); }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Promise<CollectionList<TableData>> upsert(UpsertOptions options) {
        if (((CollectionList<TableData>) Objects.requireNonNull(await(findAll(options), null))).size() == 0) {
            return async(o -> ICollectionList.ArrayOf(insert(options).complete()));
        } else {
            return update(options);
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
                    String sql = "insert into " + getName() + " (" + columns + ") values (" + values + ")" + ";";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    options.getValues().valuesList().foreach((o, i) -> {
                        try {
                            statement.setObject(i+1, o);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    statement.executeUpdate();
                    return new TableData(Table.this, connection, getDefinitions(), options.getValues(), sql);
                } catch (CommunicationsException e2) {
                    try {
                        sequelize.authenticate();
                        return null;
                    } catch (SQLException e3) {
                        throw new RuntimeException(e3);
                    }
                } catch (SQLException e) { throw new RuntimeException(e); }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Promise<CollectionList<TableData>> delete(@Nullable FindOptions options) {
        if (options == null) options = new FindOptions.Builder().addWhere("true", true).build();
        Validate.isTrue(options.where() != null && Objects.requireNonNull(options.where()).size() != 0, "FindOptions(with where clause) must be provided.");
        FindOptions finalOptions = options;
        return new Promise<CollectionList<TableData>>() {
            @SuppressWarnings("unchecked")
            @Override
            public CollectionList<TableData> apply(Object o) {
                try {
                    CollectionList<TableData> dataList = (CollectionList<TableData>) await(findAll(finalOptions), null);
                    StringBuilder sb = new StringBuilder("delete from " + getName());
                    if (finalOptions.where() != null) {
                        sb.append(" where ");
                        Objects.requireNonNull(finalOptions.where()).forEach((k, v) -> sb.append(k).append("=? "));
                    } else throw new IllegalArgumentException("Where clause must be provided.");
                    if (finalOptions.limit() != null) sb.append(" limit ").append(finalOptions.limit()).append(" ");
                    sb.append(";");
                    PreparedStatement statement = connection.prepareStatement(sb.toString());
                    AtomicReference<SQLException> exception = new AtomicReference<>();
                    new CollectionList<>(Objects.requireNonNull(finalOptions.where()).values()).foreach((o2, i) -> {
                        if (exception.get() != null) return;
                        try {
                            statement.setObject(1 + i, o2);
                        } catch (SQLException t) {
                            exception.set(t);
                        }
                    });
                    if (exception.get() != null) throw exception.get();
                    statement.executeUpdate();
                    return dataList;
                } catch (CommunicationsException e2) {
                    try {
                        sequelize.authenticate();
                        return null;
                    } catch (SQLException e3) {
                        throw new RuntimeException(e3);
                    }
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
            if (data == null) throw new NullPointerException();
            data.forEach(t -> options.getFieldsMap().forEach((k, i) -> t.update(k, t.getInteger(k) + i, options).complete()));
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
            data.forEach(t -> options.getFieldsMap().forEach((k, i) -> awaitT(t.update(k, t.get(k, Integer.class) - i, options))));
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
            } catch (CommunicationsException e2) {
                try {
                    sequelize.authenticate();
                    return null;
                } catch (SQLException e3) {
                    throw new RuntimeException(e3);
                }
            } catch (SQLException e) { throw new RuntimeException(e); }
        });
    }

    @Override
    public String toString() {
        return "Table{name='" + getName() + "',fields=" + tableData.size() + ",connection='" + connection.toString() + "'}";
    }
}
