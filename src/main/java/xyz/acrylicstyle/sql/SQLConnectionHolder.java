package xyz.acrylicstyle.sql;

import java.sql.Connection;

public interface SQLConnectionHolder {
    /**
     * Returns the current sql connection. May be null if never authenticated with database.
     * @return the database connection, may be null if never authenticated with database
     */
    Connection getConnection();
}
