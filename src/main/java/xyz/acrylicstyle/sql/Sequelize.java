package xyz.acrylicstyle.sql;

import util.CollectionList;
import util.ICollectionList;
import util.StringCollection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class Sequelize implements ISQLUtils {
    public static final Pattern TABLE_NAME_REGEX = Pattern.compile("^[A-Za-z]([A-Za-z0-9_]){0,63}$");
    public static final Pattern FIELD_NAME_REGEX = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private Connection connection;
    private String url;
    private String user;
    private String password;
    private StringCollection<TableDefinition[]> definitions = new StringCollection<>();

    public static void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find com.mysql.cj.jdbc.Driver class!");
        }
    }

    public Sequelize(String host, String database, String user, String password) {
        this.url =  "jdbc:mysql://" + Validate.notNull(host, "Host cannot be null") + "/" + Validate.notNull(database, "Database cannot be null");
        this.user = Validate.notNull(user, "User cannot be null");
        this.password = Validate.notNull(password, "Password cannot be null");
    }

    public Sequelize(String url, String user, String password) {
        this.url = Validate.notNull(url, "URL cannot be null");
        this.user = Validate.notNull(user, "User cannot be null");
        this.password = Validate.notNull(password, "Password cannot be null");
    }

    public Sequelize(String url) {
        this.url = Validate.notNull(url, "URL cannot be null");
    }

    @Override
    public Connection authenticate() throws SQLException {
        connection = DriverManager.getConnection(this.url, this.user, this.password);
        return connection;
    }

    @Override
    public void ping() throws SQLException {
        try {
            Statement statement = Validate.notNull(connection, "Connection hasn't made yet.").createStatement();
            statement.execute("select 1;");
        } catch (SQLException e) {
            authenticate();
        }
    }

    @Override
    public void close() throws SQLException {
        Validate.notNull(connection, "Connection hasn't made yet.").close();
        connection = null;
    }

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
                CollectionList<TableDefinition> primaryKeys = ICollectionList.asList(definitionArr).filter(TableDefinition::isPrimaryKey);
                if (primaryKeys.size() > 1) throw new IllegalArgumentException("Table " + table + " cannot have primary key more than 1.");
                TableDefinition primaryKey = primaryKeys.size() == 0 ? null : primaryKeys.first();
                Statement statement = connection.createStatement();
                StringBuilder sb = new StringBuilder();
                sb.append("create table ").append(force ? "" : "if not exists").append(" ").append(table).append(" (");
                ICollectionList.asList(definitionArr).foreach((def, index, list) -> {
                    sb
                            .append(def.getName())
                            .append(" ")
                            .append(def.getType().getType())
                            .append(def.allowNull() ? " " : " not null ");
                    if (def.getDefaultValue() != null) sb
                            .append("default ")
                            .append(def.getDefaultValue());
                    if (def.isAutoIncrement()) sb.append(" auto_increment");
                    sb.append((index + 1) == list.size() && primaryKey == null ? "" : ",");
                });
                if (primaryKey != null) sb.append("primary key (").append(primaryKey.getName()).append(")");
                sb.append(");");
                statement.executeUpdate(sb.toString());
            } catch (SQLException e) {
                ex.set(e);
            }
        });
        if (ex.get() != null) throw ex.get();
    }

    @Override
    public Table define(String table, TableDefinition[] definitionArr) {
        Validate.notNull(table, "Table cannot be null");
        Validate.notNull(definitionArr, "Definitions cannot be null");
        Validate.isTrue(table.matches(TABLE_NAME_REGEX.pattern()), "Table " + table + " must match following pattern: " + TABLE_NAME_REGEX.pattern());
        if (definitions.get(table) != null) throw new IllegalStateException("Table " + table + " already exists!");
        StringCollection<TableDefinition> definitions = new StringCollection<>();
        for (TableDefinition def : definitionArr) {
            Validate.isTrue(def.getName().matches(FIELD_NAME_REGEX.pattern()), "Field " + def.getName() + " must match following pattern: " + FIELD_NAME_REGEX.pattern());
            definitions.add(def.getName(), def);
        }
        this.definitions.add(table, definitionArr);
        return new Table(table, definitions, connection);
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}
