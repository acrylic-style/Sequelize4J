package xyz.acrylicstyle.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.CollectionList;
import util.promise.rewrite.Promise;
import xyz.acrylicstyle.sql.options.FindOptions;
import xyz.acrylicstyle.sql.options.IncrementOptions;
import xyz.acrylicstyle.sql.options.InsertOptions;
import xyz.acrylicstyle.sql.options.UpsertOptions;

public interface ITable extends SQLConnectionHolder {
    String getName();

    /**
     * Finds multiple datas from table.
     * @param options FindOptions. Required.
     * @return Found table datas. Can be converted into array using {@link CollectionList#toArray(Object[])} or {@link CollectionList#toArray()}
     */
    @NotNull
    Promise<@NotNull CollectionList<@NotNull TableData>> findAll(FindOptions options);

    /**
     * Finds a data from table. If multiple data was found, the first data will be returned.
     * @param options FindOptions. Required.
     * @return Found table data.
     */
    @NotNull
    Promise<@Nullable TableData> findOne(FindOptions options);

    /**
     * Updates a data.
     * @param field Name of field
     * @param value Value
     * @param options Find Options. Required.
     * @return Updated table data.
     */
    @NotNull
    Promise<@NotNull CollectionList<@NotNull TableData>> update(String field, Object value, FindOptions options);

    /**
     * Updates multiple data.
     * @param options Options that contains values, and where clause.
     * @return Updated table data list.
     */
    Promise<CollectionList<TableData>> update(UpsertOptions options);

    /**
     * Insert a data if not exists, Update a data if exists.
     * @param options Upsert Options. Required.
     * @return Created or Updated Table data list.
     */
    @NotNull
    Promise<@NotNull CollectionList<@NotNull TableData>> upsert(UpsertOptions options);

    /**
     * Insert a data into table.
     * @param options Insert options. Required.
     * @return Created table data.
     */
    @NotNull
    Promise<@NotNull TableData> insert(InsertOptions options);

    /**
     * Drops(Delete) entire table.<br>
     * <b>This action cannot be undone!</b>
     */
    @NotNull
    Promise<Void> drop();

    /**
     * Delete a data from table.
     * <b>This action cannot be undone!</b>
     * @param options FindOptions. Required.
     * @return Deleted rows
     */
    @NotNull
    Promise<@NotNull CollectionList<@NotNull TableData>> delete(FindOptions options);

    /**
     * Increase a value by specified value.
     */
    @NotNull
    Promise<Void> increment(IncrementOptions options);

    /**
     * Decrease a value by specified value.
     */
    @NotNull
    Promise<Void> decrement(IncrementOptions options);
}
