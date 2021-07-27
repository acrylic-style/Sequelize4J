package xyz.acrylicstyle.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.CollectionList;
import util.ICollectionList;
import util.StringCollection;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class Sequelize implements ISQLUtils {
    public static final Pattern TABLE_NAME_REGEX = Pattern.compile("^[A-Za-z]([A-Za-z0-9_]){0,63}$");
    public static final Pattern FIELD_NAME_REGEX = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private Connection connection;
    public final String url;
    private String user;
    private String password;
    private final StringCollection<TableDefinition[]> definitions = new StringCollection<>();

    @NotNull
    public String getURL() { return url; }

    @Nullable
    public String getUser() { return user; }

    /**
     * Returns database password. You must always keep this secret.
     */
    @Nullable
    public String getPassword() { return password; }

    public Sequelize(@NotNull String host, @NotNull String database, @NotNull String user, @NotNull String password) {
        this.url =  "jdbc:mysql://" + Validate.notNull(host, "Host cannot be null") + "/" + Validate.notNull(database, "Database cannot be null");
        this.user = Validate.notNull(user, "User cannot be null");
        this.password = Validate.notNull(password, "Password cannot be null");
    }

    public Sequelize(@NotNull String url, @NotNull String user, @NotNull String password) {
        this.url = Validate.notNull(url, "URL cannot be null");
        this.user = Validate.notNull(user, "User cannot be null");
        this.password = Validate.notNull(password, "Password cannot be null");
    }

    public Sequelize(@NotNull String url) { this.url = Validate.notNull(url, "URL cannot be null"); }

    /**
     * Returns MySQL jdbc driver.
     * Requires mysql:mysql-connector-java.
     */
    @Nullable
    public static Driver getMySQLDriver() {
        Driver driver = null;
        try {
            driver = (Driver) Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (ReflectiveOperationException ignored) {}
        if (driver == null) {
            try {
                driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
            } catch (ReflectiveOperationException ignored) {}
        }
        return driver;
    }

    /**
     * Returns MariaDB jdbc driver.
     * Requires org.mariadb.jdbc:mariadb-java-client.
     */
    @Nullable
    public static Driver getMariaDBDriver() {
        Driver driver = null;
        try {
            driver = (Driver) Class.forName("org.mariadb.jdbc.Driver").newInstance();
        } catch (ReflectiveOperationException ignored) {}
        return driver;
    }

    /**
     * Returns MariaDB jdbc driver.
     * Requires org.xerial:sqlite-jdbc.
     */
    @Nullable
    public static Driver getSQLiteDriver() {
        Driver driver = null;
        try {
            driver = (Driver) Class.forName("org.sqlite.JDBC").newInstance();
        } catch (ReflectiveOperationException ignored) {}
        return driver;
    }

    /**
     * Creates connection between database.
     * @param url an database URL
     * @param properties a properties (you may need to provide credentials via properties)
     * @return sequelize instance
     * @throws SQLException If could not connect to the database
     */
    @NotNull
    public static Sequelize connect(@NotNull String url, @Nullable Properties properties) throws SQLException {
        Sequelize sequelize = new Sequelize(url);
        sequelize.authenticate(properties);
        return sequelize;
    }

    /**
     * Creates connection between database.
     * @param driver a driver that will be used to create connection to the database.
     * @param url an database URL
     * @param properties a properties (you may need to provide credentials via properties)
     * @return sequelize instance
     * @throws SQLException If could not connect to the database
     */
    @NotNull
    public static Sequelize connect(@Nullable Driver driver, @NotNull String url, @Nullable Properties properties) throws SQLException {
        Sequelize sequelize = new Sequelize(url);
        sequelize.authenticate(driver, properties);
        return sequelize;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Connection authenticate() throws SQLException {
        return authenticate(null, null);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Connection authenticate(@Nullable Driver driver) throws SQLException {
        return authenticate(driver, null);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Connection authenticate(@Nullable Properties properties) throws SQLException {
        return authenticate(null, properties);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Connection authenticate(@Nullable Driver driver, @Nullable Properties p) throws SQLException {
        Properties properties = p == null ? new Properties() : p;
        if (this.user != null) properties.setProperty("user", this.user);
        if (this.password != null) properties.setProperty("password", this.password);
        properties.put("maxReconnects", properties.getOrDefault("maxReconnects", 1));
        properties.put("autoReconnect", properties.getOrDefault("autoReconnect", true));
        StringBuilder sb = new StringBuilder("?");
        AtomicBoolean first = new AtomicBoolean(false);
        properties.forEach((o1, o2) -> {
            if (first.get()) sb.append('&');
            sb.append(o1).append("=").append(o2);
            first.set(true);
        });
        if (this.url.endsWith(":")) {
            if (driver == null) {
                connection = DriverManager.getConnection(this.url, properties);
            } else {
                connection = driver.connect(this.url, properties);
            }
        } else {
            if (driver == null) {
                connection = DriverManager.getConnection(this.url + sb);
            } else {
                connection = driver.connect(this.url, properties);
            }
        }
        return connection;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("SqlNoDataSourceInspection")
    @Override
    public void ping() throws SQLException {
        if (connection.isClosed() || !connection.isValid(3000)) {
            authenticate();
            return;
        }
        try {
            if (connection == null) throw new IllegalStateException("Connection hasn't made yet.");
            Statement statement = connection.createStatement();
            statement.execute("select 1;");
        } catch (Exception e) {
            System.err.println("An error occurred while pinging, reconnecting to the database. (you may safely ignore this error unless it's working incorrectly)");
            e.printStackTrace();
            authenticate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws SQLException {
        Validate.notNull(connection, "Connection hasn't made yet.").close();
        connection = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sync() throws SQLException {
        sync(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sync(boolean force) throws SQLException {
        Validate.notNull(connection, "Connection hasn't made yet.");
        AtomicReference<SQLException> ex = new AtomicReference<>();
        definitions.forEach((table, definitionArr) -> {
            try {
                ICollectionList<TableDefinition> list = ICollectionList.asList(definitionArr);
                ICollectionList<TableDefinition> primaryKeys = list.filter(TableDefinition::isPrimaryKey);
                // primary key amount check
                if (primaryKeys.size() > 1) throw new IllegalArgumentException("Table " + table + " cannot have primary key more than 1");
                TableDefinition primaryKey = primaryKeys.size() == 0 ? null : primaryKeys.first();
                StringBuilder sb = new StringBuilder();
                sb.append("create table ").append(force ? "" : "if not exists").append(" `").append(table).append("` (");
                CollectionList<Object> values = new CollectionList<>();
                list.foreach((def, index) -> {
                    sb.append('`').append(def.getName()).append('`')
                            .append(" ")
                            .append(def.getType().getType())
                            .append(def.allowNull() ? " " : " not null ");
                    if (def.getDefaultValue() != null) {
                        sb.append("default ?");
                        values.add(def.getDefaultValue());
                    }
                    if (def.isAutoIncrement()) sb.append(" auto_increment");
                    sb.append((index + 1) == list.size() && primaryKey == null ? "" : ",");
                });
                // set primary key if any
                if (primaryKey != null) sb.append("primary key (`").append(primaryKey.getName()).append("`)");
                sb.append(");");
                PreparedStatement statement = connection.prepareStatement(sb.toString());
                values.foreach((o, index) -> {
                    if (ex.get() != null) return;
                    try {
                        statement.setObject(index + 1, o);
                    } catch (SQLException se) {
                        ex.set(se);
                    }
                });
                if (ex.get() != null) throw ex.get();
                statement.executeUpdate();
                statement.close();
            } catch (SQLException e) {
                ex.set(e);
            }
        });
        if (ex.get() != null) throw ex.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Table define(String table, TableDefinition[] definitionArr) {
        Validate.notNull(table, "Table cannot be null");
        Validate.notNull(definitionArr, "Definitions cannot be null");
        Validate.isTrue(table.matches(TABLE_NAME_REGEX.pattern()), "Table " + table + " must match following pattern: " + TABLE_NAME_REGEX.pattern());
        if (definitions.get(table) != null) throw new IllegalStateException("Table " + table + " already exists");
        StringCollection<TableDefinition> definitions = new StringCollection<>();
        AtomicReference<TableDefinition> primaryKey = new AtomicReference<>();
        for (TableDefinition def : definitionArr) {
            Validate.isTrue(def.getName().matches(FIELD_NAME_REGEX.pattern()), "Field " + def.getName() + " must match following pattern: " + FIELD_NAME_REGEX.pattern());
            if (def.isPrimaryKey()) primaryKey.set(def);
            if (!(def.getType() instanceof ArrayDataType)) {
                definitions.add(def.getName(), def);
            }
        }
        for (TableDefinition def : definitionArr) {
            if (def.getType() instanceof ArrayDataType) {
                if (primaryKey.get() == null) throw new IllegalArgumentException("Primary key is required for ArrayDataType");
                if (primaryKey.get() == def) throw new IllegalArgumentException("Primary key cannot be equals with ArrayDataType");
                define(def.getName() + "_array", new TableDefinition[] {
                        primaryKey.get().copy("k"),
                        new TableDefinition.Builder("v", ((ArrayDataType<?>) def.getType()).arrayType)
                                .setDefaultValue(def.getDefaultValue())
                                .setAllowNull(def.allowNull())
                                .setPrimaryKey(def.isPrimaryKey())
                                .setAutoIncrement(def.isAutoIncrement())
                                .build(),
                });
            }
        }
        this.definitions.add(table, definitionArr);
        return new Table(table, definitions, connection, this);
    }

    @Override
    public Connection getConnection() { return connection; }

    @Override
    public @NotNull StringCollection<TableDefinition[]> getDefinitions() {
        return definitions;
    }
}
