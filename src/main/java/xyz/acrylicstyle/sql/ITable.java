package xyz.acrylicstyle.sql;

import util.CollectionList;
import xyz.acrylicstyle.sql.options.*;

import java.sql.SQLException;

public interface ITable extends IUtils {
    String getName();

    /**
     * Finds multiple datas from table.
     * @param options FindOptions. Required.
     * @return Found table datas. Can be converted into array using {@link CollectionList#toArray(Object[])} or {@link CollectionList#toArray()}
     * @throws SQLException When SQL action fails
     */
    CollectionList<TableData> findAll(FindOptions options) throws SQLException;

    /**
     * Finds a data from table. If multiple data was found, the first data will be returned.
     * @param options FindOptions. Required.
     * @return Found table data.
     * @throws SQLException When SQL action fails
     */
    TableData findOne(FindOptions options) throws SQLException;

    /**
     * Updates a data.
     * @param field Name of field
     * @param value Value
     * @param options Find Options. Required.
     * @return Updated table data.
     * @throws SQLException When couldn't do sql stuff for some reason
     */
    TableData update(String field, Object value, FindOptions options) throws SQLException;

    /**
     * Updates multiple data.
     * @param field Name of field
     * @param options Options that contains values, and where clause.
     * @return Updated table data.
     * @throws SQLException When SQL action fails
     */
    TableData update(String field, UpsertOptions options) throws SQLException;

    /**
     * Insert a data if not exists, Update a data if exists.
     * @param field Name of field
     * @param options Upsert Options. Required.
     * @return Created or Updated Table data.
     * @throws SQLException When couldn't do sql stuff for some reason
     */
    TableData upsert(String field, UpsertOptions options) throws SQLException;

    /**
     * Insert a data into table.
     * @param field Name of field.
     * @param options Insert options. Required.
     * @return Created table data.
     * @throws SQLException When couldn't do sql stuff for some reason
     */
    TableData insert(String field, InsertOptions options) throws SQLException;

    /**
     * Drops(Delete) entire table.<br>
     * <b>This action cannot be undone!</b>
     */
    void drop() throws SQLException;

    /**
     * Delete a data from table.
     * <b>This action cannot be undone!</b>
     * @param options FindOptions. Required.
     * @return Deleted table data.
     * @throws SQLException When SQL action fails
     */
    TableData delete(FindOptions options) throws SQLException;
}
