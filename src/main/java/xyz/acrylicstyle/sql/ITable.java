package xyz.acrylicstyle.sql;

import util.CollectionList;
import xyz.acrylicstyle.sql.options.*;

import java.sql.SQLException;

public interface ITable extends IUtils {
    String getName();
    TableData set(String field, Object value, FindOptions options) throws SQLException;
    // TableData increment(IncrementOptions options) throws SQLException;
    CollectionList<TableData> findAll(String field, FindOptions options) throws SQLException;
    TableData findOne(String field, FindOptions options) throws SQLException;
    TableData update(String field, Object value, FindOptions options) throws SQLException;
    TableData upsert(String field, Object value, UpsertOptions options) throws SQLException;

    /**
     * Drops(Delete) entire table.
     */
    void drop() throws SQLException;
    TableData delete(FindOptions options) throws SQLException;
}
