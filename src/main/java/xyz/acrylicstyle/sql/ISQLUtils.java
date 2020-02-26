package xyz.acrylicstyle.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface ISQLUtils extends IUtils {
    Connection authenticate() throws SQLException;
    void ping() throws SQLException;
    void close() throws SQLException;
    /**
     * Sync all defined models to the DB.
     * @param force Set to true if you want to drop all defined tables.
     */
    void sync(boolean force) throws SQLException;
    Table define(String table, TableDefinition[] definition);
}
