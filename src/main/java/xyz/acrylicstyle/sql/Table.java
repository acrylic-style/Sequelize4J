package xyz.acrylicstyle.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.collection.Collection;
import util.collection.CollectionList;
import util.collection.ICollection;
import util.collection.ICollectionList;
import util.collection.StringCollection;
import util.eventEmitter.TypedEventEmitter;
import util.promise.rewrite.Promise;
import xyz.acrylicstyle.sql.options.FindOptions;
import xyz.acrylicstyle.sql.options.IncrementOptions;
import xyz.acrylicstyle.sql.options.InsertOptions;
import xyz.acrylicstyle.sql.options.Ops;
import xyz.acrylicstyle.sql.options.UpsertOptions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Table implements ITable {
    private final String name;
    private final StringCollection<TableDefinition> tableData;
    private final Connection connection;
    private final Sequelize sequelize;
    private final TypedEventEmitter<Events> eventEmitter = new TypedEventEmitter<>();

    Table(String name, StringCollection<TableDefinition> tableData, Connection connection, Sequelize sequelize) {
        this.name = name;
        this.tableData = tableData;
        this.connection = connection;
        this.sequelize = sequelize;
    }

    @NotNull
    public TypedEventEmitter<Events> getEventEmitter() {
        return eventEmitter;
    }

    @NotNull
    public Sequelize getSequelize() {
        return sequelize;
    }

    @NotNull
    public TableDefinition getDefinition(String field) { return tableData.get(field); }

    @NotNull
    public StringCollection<TableDefinition> getDefinitions() { return tableData; }

    @NotNull
    public String getName() { return name; }

    @NotNull
    public Connection getConnection() { return connection; }

    @Override
    public @NotNull Promise<@NotNull CollectionList<@NotNull TableData>> findAll(@Nullable FindOptions options) {
        return Promise.create("Sequelize Thread Pool (findAll) #%d", context -> {
            try {
                StringBuilder sb = new StringBuilder("SELECT * FROM `" + getName() + "`");
                CollectionList<Object> values = new CollectionList<>();
                if (options != null) {
                    Map<String, Map.Entry<Ops, Object>> where = options.where();
                    if (where != null && where.size() != 0) {
                        sb.append(" WHERE ");
                        if (options == FindOptions.ALL) {
                            sb.append("true");
                        } else {
                            sb.append(new CollectionList<>(where.keySet()).map(s -> "`" + s + "` " + where.get(s).getKey().op + " ?").join(" AND ")).append(" ");
                            values.addAll(where.values().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
                        }
                    }
                    if (options.orderBy() != null && !Objects.equals(options.orderBy(), "")) {
                        sb.append(" ORDER BY `").append(options.orderBy()).append("` ").append(options.order().name());
                    }
                    if (options.limit() != null) sb.append(" LIMIT ").append(options.limit());
                }
                Thread.currentThread().setName(Thread.currentThread().getName() + " (" + sb + ")");
                eventEmitter.emit(Events.EXECUTE, sb.toString());
                long start = System.currentTimeMillis();
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
                result.close();
                statement.close();
                eventEmitter.emit(Events.EXECUTED, sb.toString(), System.currentTimeMillis() - start);
                context.resolve(tableData);
            } catch (SQLException e) {
                context.reject(new RuntimeException(e));
            }
        });
    }

    @Override
    public @NotNull Promise<@Nullable TableData> findOne(FindOptions options) {
        return Promise.create("Sequelize Thread Pool (findOne) #%d", context -> {
            CollectionList<TableData> list = findAll(options).complete();
            context.resolve(list.size() == 0 ? null : list.first());
        });
    }

    @Override
    public @NotNull Promise<@NotNull CollectionList<@NotNull TableData>> update(String field, Object value, FindOptions options) {
        Validate.isTrue(field.matches(Sequelize.FIELD_NAME_REGEX.pattern()), "Field " + field + " must match following pattern: " + Sequelize.FIELD_NAME_REGEX.pattern());
        return Promise.create("Sequelize Thread Pool (update) #%d", context -> {
            try {
                CollectionList<TableData> dataList = findAll(options).complete();
                StringBuilder sb = new StringBuilder("UPDATE `" + getName() + "` SET `" + field + "`=?");
                CollectionList<Object> values = new CollectionList<>();
                if (options != null && options.where() != null) {
                    sb.append(" WHERE ");
                    if (options == FindOptions.ALL) {
                        sb.append("true");
                    } else {
                        ICollection.asCollection(options.where()).forEach((k, v, i, a) -> {
                            values.add(v.getValue());
                            sb.append("`").append(k).append("`").append(v.getKey().op).append("?");
                            if (i != 0) sb.append(" AND ");
                        });
                    }
                }
                Thread.currentThread().setName(Thread.currentThread().getName() + " (" + sb + ")");
                eventEmitter.emit(Events.EXECUTE, sb.toString());
                long start = System.currentTimeMillis();
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
                statement.close();
                eventEmitter.emit(Events.EXECUTED, sb.toString(), System.currentTimeMillis() - start);
                context.resolve(dataList.map(td -> {
                    Map<String, Object> values2 = td.getValues();
                    values2.put(field, value);
                    td.setValues(values2);
                    return td;
                }));
            } catch (SQLException e) {
                context.reject(new RuntimeException(e));
            }
        });
    }

    @Override
    public @NotNull Promise<@NotNull CollectionList<@NotNull TableData>> update(@NotNull UpsertOptions options) {
        Validate.isTrue(options.getValues() != null && options.getValues().size() != 0, "Values must be specified.");
        return Promise.create("Sequelize Thread Pool (update) #%d", context -> {
            try {
                CollectionList<TableData> dataList = findAll(options).complete();
                StringBuilder sb = new StringBuilder("UPDATE `" + getName() + "` SET ");
                List<Object> objects = new ArrayList<>();
                new Collection<>(options.getValues()).forEach((key, value, i, a) -> {
                    if (i > 0) sb.append(", ");
                    sb.append("`").append(key).append("` = ?");
                    objects.add(value);
                });
                @Nullable final Map<String, Map.Entry<Ops, Object>> where = options.where();
                if (where != null) {
                    sb.append(" WHERE ");
                    sb.append(new CollectionList<>(where.keySet()).map(s -> "`" + s + "` " + where.get(s).getKey().op + " ?").join(" AND ")).append(" ");
                }
                Thread.currentThread().setName(Thread.currentThread().getName() + " (" + sb + ")");
                eventEmitter.emit(Events.EXECUTE, sb.toString());
                long start = System.currentTimeMillis();
                PreparedStatement statement = connection.prepareStatement(sb.toString());
                AtomicInteger index = new AtomicInteger();
                objects.forEach(o -> {
                    try {
                        statement.setObject(index.incrementAndGet(), o);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                if (where != null) {
                    where.values().forEach(o -> {
                        try {
                            statement.setObject(index.incrementAndGet(), o.getValue());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                }
                statement.executeUpdate();
                statement.close();
                eventEmitter.emit(Events.EXECUTED, sb.toString(), System.currentTimeMillis() - start);
                context.resolve(dataList.map(td -> {
                    td.setValues(options.getValues());
                    return td;
                }));
            } catch (SQLException e) {
                context.reject(new RuntimeException(e));
            }
        });
    }

    @Override
    public @NotNull Promise<@NotNull CollectionList<@NotNull TableData>> upsert(UpsertOptions options) {
        return Promise.create("Sequelize Thread Pool (upsert) #%d", context -> {
            if (findAll(options).complete().size() == 0) {
                context.resolve(ICollectionList.of(insert(options).complete()));
            } else {
                context.resolve(update(options).complete());
            }
        });
    }

    @Override
    public @NotNull Promise<@NotNull TableData> insert(InsertOptions options) {
        Validate.isTrue(options != null && options.getValues() != null && options.getValues().size() != 0, "InsertOptions must not be null and has 1 key/value at least.");
        return Promise.create("Sequelize Thread Pool (insert) #%d", context -> {
            try {
                List<Object> objects = new ArrayList<>();
                StringBuilder sb = new StringBuilder("INSERT INTO `").append(getName()).append("` (");
                new Collection<>(options.getValues()).forEach((key, value, i, a) -> {
                    if (i > 0) sb.append(", ");
                    sb.append("`").append(key).append("`");
                    objects.add(value);
                });
                sb.append(") VALUES (").append(new CollectionList<>(options.getValues().values()).map(s -> "?").join(", ")).append(")");
                Thread.currentThread().setName(Thread.currentThread().getName() + " (" + sb + ")");
                eventEmitter.emit(Events.EXECUTE, sb.toString());
                long start = System.currentTimeMillis();
                PreparedStatement statement = connection.prepareStatement(sb.toString());
                AtomicInteger i = new AtomicInteger();
                objects.forEach(o -> {
                    try {
                        statement.setObject(i.incrementAndGet(), o);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                statement.executeUpdate();
                statement.close();
                eventEmitter.emit(Events.EXECUTED, sb.toString(), System.currentTimeMillis() - start);
                context.resolve(new TableData(Table.this, connection, getDefinitions(), options.getValues(), sb.toString()));
            } catch (SQLException e) {
                context.reject(new RuntimeException(e));
            }
        });
    }

    /**
     * {@inheritDoc}
     * If you (really) want to delete everything, use {@link FindOptions#ALL}.
     */
    @Override
    public @NotNull Promise<@NotNull CollectionList<@NotNull TableData>> delete(@NotNull FindOptions options) {
        //noinspection ConstantConditions
        if (options == null) throw new IllegalArgumentException("FindOptions must be provided. (If you meant to delete everything, use FindOptions#ALL.)");
        Validate.isTrue(options.where() != null && Objects.requireNonNull(options.where()).size() != 0, "FindOptions(with where clause) must be provided.");
        return Promise.create("Sequelize Thread Pool (delete) #%d", context -> {
            try {
                CollectionList<TableData> dataList = findAll(options).complete();
                StringBuilder sb = new StringBuilder("DELETE FROM `" + getName() + "`");
                CollectionList<Object> values = new CollectionList<>();
                Map<String, Map.Entry<Ops, Object>> where = options.where();
                if (where != null) {
                    sb.append(" WHERE ");
                    if (options == FindOptions.ALL) {
                        sb.append("true");
                    } else {
                        values.addAll(where.values().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
                        sb.append(new CollectionList<>(where.keySet()).map(s -> "`" + s + "`" + where.get(s).getKey().op + "?").join(" AND ")).append(" ");
                    }
                } else throw new IllegalArgumentException("Where clause must be provided.");
                if (options.limit() != null) sb.append(" LIMIT ").append(options.limit()).append(" ");
                Thread.currentThread().setName(Thread.currentThread().getName() + " (" + sb + ")");
                eventEmitter.emit(Events.EXECUTE, sb.toString());
                long start = System.currentTimeMillis();
                PreparedStatement statement = connection.prepareStatement(sb.toString());
                AtomicReference<SQLException> exception = new AtomicReference<>();
                values.foreach((o2, i) -> {
                    if (exception.get() != null) return;
                    try {
                        statement.setObject(1 + i, o2);
                    } catch (SQLException t) {
                        exception.set(t);
                    }
                });
                if (exception.get() != null) throw exception.get();
                statement.executeUpdate();
                statement.close();
                eventEmitter.emit(Events.EXECUTED, sb.toString(), System.currentTimeMillis() - start);
                context.resolve(dataList);
            } catch (SQLException e) {
                context.reject(new RuntimeException(e));
            }
        });
    }

    @Override
    public @NotNull Promise<Void> increment(@NotNull IncrementOptions options) {
        Validate.isTrue(options.getFieldsMap() != null && options.getFieldsMap().size() != 0, "IncrementOptions(with fieldsMap) must be provided.");
        return Promise.create("Sequelize Thread Pool (increment) #%d", context -> {
            CollectionList<TableData> data = findAll(options).complete();
            data.forEach(t -> options.getFieldsMap().forEach((k, i) -> t.update(k, t.getInteger(k) + i, options).complete()));
            context.resolve(null);
        });
    }

    @Override
    public @NotNull Promise<Void> decrement(@NotNull IncrementOptions options) {
        Validate.isTrue(options.getFieldsMap() != null && options.getFieldsMap().size() != 0, "IncrementOptions(with fieldsMap) must be provided.");
        return Promise.create("Sequelize Thread Pool (decrement) #%d", context -> {
            CollectionList<TableData> data = findAll(options).complete();
            data.forEach(t -> options.getFieldsMap().forEach((k, i) -> t.update(k, t.get(k, Integer.class) - i, options).complete()));
            context.resolve(null);
        });
    }

    @Override
    public @NotNull Promise<Void> drop() {
        return Promise.create("Sequelize Thread Pool (drop) #%d", context -> {
            try {
                String sql = "DROP TABLE IF EXISTS `" + getName() + "`";
                eventEmitter.emit(Events.EXECUTE, sql);
                long start = System.currentTimeMillis();
                connection.createStatement().executeUpdate(sql);
                eventEmitter.emit(Events.EXECUTED, sql, System.currentTimeMillis() - start);
                context.resolve(null);
            } catch (SQLException e) {
                context.reject(new RuntimeException(e));
            }
        });
    }

    @Override
    public String toString() {
        return "Table{name='" + getName() + "',fields=" + tableData.size() + ",connection='" + connection.toString() + "'}";
    }

    public enum Events {
        /**
         * Fired when the query is about to be executed (pre-execute).
         * Arguments: [{@link String} (sql)]
         */
        EXECUTE,
        /**
         * Fired when the query was executed (post-execute).
         * Arguments: [{@link String} (sql), long (time took to execute)]
         */
        EXECUTED,
    }
}
