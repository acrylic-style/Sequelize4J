package xyz.acrylicstyle.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.StringCollection;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public interface ISQLUtils extends SQLConnectionHolder {
    /**
     * Creates connection between database and authenticates with database, using default driver and default settings.
     * @return connection
     * @throws SQLException If connection could not be made or could not authenticate with database
     */
    @NotNull
    Connection authenticate() throws SQLException;

    /**
     * Creates connection between database and authenticates with database, using specified driver and default settings.
     * @param driver a driver that will be used to create connection to the database.
     * @return connection
     * @throws SQLException If connection could not be made or could not authenticate with database
     */
    @NotNull
    Connection authenticate(@Nullable Driver driver) throws SQLException;

    /**
     * Creates connection between database and authenticates with database, using default driver and specified settings.
     * @param properties a settings that will be applied to the connection.
     * @return connection
     * @throws SQLException If connection could not be made or could not authenticate with database
     */
    @NotNull
    Connection authenticate(@Nullable Properties properties) throws SQLException;

    /**
     * Creates connection between database and authenticates with database, using specified driver and specified settings.
     * @param properties a settings that will be applied to the connection.
     * @param driver a driver that will be used to create connection to the database.
     * @return connection
     * @throws SQLException If connection could not be made or could not authenticate with database
     */
    @NotNull
    Connection authenticate(@Nullable Driver driver, @Nullable Properties properties) throws SQLException;

    /**
     * Pings(tests) the connection and reconnect if it has the connection that is no longer valid.
     * @throws SQLException If unknown error occurred while pinging connection
     */
    void ping() throws SQLException;

    /**
     * Closes the database connection.
     * @throws SQLException If connection was never made or an error occurred while disconnecting from server.
     */
    void close() throws SQLException;

    /**
     * Sync all defined models to the DB.
     */
    void sync() throws SQLException;

    /**
     * Sync all defined models to the DB.
     * @param force Set to true if you want to drop all defined tables.
     */
    void sync(boolean force) throws SQLException;

    /**
     * Defines table.
     * @param table a table name
     * @param definition a table definitions
     * @return table
     */
    @NotNull
    Table define(String table, TableDefinition[] definition);

    /**
     * Returns the database definitions that is mapped with table name, and table definitions.
     * @return table definitions (columns)
     */
    @NotNull
    StringCollection<TableDefinition[]> getDefinitions();
}
