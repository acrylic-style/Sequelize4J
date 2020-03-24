package xyz.acrylicstyle.sql;

import util.CollectionList;
import util.StringCollection;
import xyz.acrylicstyle.sql.options.FindOptions;
import xyz.acrylicstyle.sql.options.IncrementOptions;
import xyz.acrylicstyle.sql.options.InsertOptions;
import xyz.acrylicstyle.sql.options.Sort;

import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import static util.promise.Promise.await;
import static util.promise.Promise.awaitT;

/**
 * An class for test how sequelize works.
 */
public class Test {
    public static Sequelize sequelize = null;
    public static StringCollection<Table> tables = new StringCollection<>();
    public static Table stats = null;
    public static UUID uuid = UUID.randomUUID();

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            initSQL();
            try {
                await(stats.insert(new InsertOptions.Builder().addValue("player", uuid.toString()).addValue("booed", false).addValue("i", 5).build()), null);
                await(stats.insert(new InsertOptions.Builder().addValue("player", UUID.randomUUID().toString()).addValue("booed", false).addValue("i", 1).build()), null);
                await(stats.insert(new InsertOptions.Builder().addValue("player", UUID.randomUUID().toString()).addValue("booed", false).addValue("i", 2).build()), null);
                await(stats.insert(new InsertOptions.Builder().addValue("player", UUID.randomUUID().toString()).addValue("booed", false).addValue("i", 10).build()), null);
                await(stats.insert(new InsertOptions.Builder().addValue("player", UUID.randomUUID().toString()).addValue("booed", false).addValue("i", -1).build()), null);
            } catch (Exception e) {
                fail("Insert into stats table", e.getMessage());
                e.printStackTrace();
                return;
            }
            pass("Insert into stats table");
            CollectionList<TableData> dataList = (CollectionList<TableData>) await(stats.findAll(null), null);
            assert dataList != null;
            check(dataList.size() == 5, "Stats size", "Size must be 5, but it was " + dataList.size());
            TableData tableData = dataList.first();
            check(tableData.getString("player").equals(uuid.toString()),
                    "Validate player",
                    "player wasn't " + tableData.getString("player") + " == " + uuid.toString());
            check(!tableData.getBoolean("booed"), "Verify booed state is false", "booed was true");
            check(tableData.getInteger("i") == 5, "Verify i is 5", "i was " + tableData.getInteger("i"));
            CollectionList<TableData> dataList2;
            try {
                dataList2 = (CollectionList<TableData>) await(tableData.update("booed", 1, new FindOptions.Builder().addWhere("player", uuid.toString()).build()), null);
            } catch (Exception e) {
                e.printStackTrace();
                fail("Update booed state to true", e.getMessage());
                return;
            }
            pass("Update booed state to true");
            assert dataList2 != null;
            TableData data2 = dataList2.first();
            check(data2.getBoolean("booed"), "Verify booed state is true", "booed was false");
            CollectionList<TableData> sortedDataList = awaitT(stats.findAll(
                    new FindOptions
                            .Builder()
                            .setOrderBy("i")
                            .setOrder(Sort.ASC)
                            .build()
            ));
            assert sortedDataList != null;
            check(sortedDataList.first().getInteger("i") == -1, "Verify the first value of sorted data list is -1", "Returned " + sortedDataList.first().getInteger("i") + " instead of -1");
            System.out.println("i: " + sortedDataList.map(d -> d.getInteger("i")).join(", "));
            CollectionList<TableData> limitedDataList = awaitT(stats.findAll(
                    new FindOptions
                            .Builder()
                            .setLimit(3)
                            .build()
            ));
            assert limitedDataList != null;
            check(limitedDataList.size() == 3, "Verify the limited data list length is 3", "Data list length was " + limitedDataList.size());
            awaitT(stats.increment(new IncrementOptions.Builder().addWhere("player", uuid.toString()).addField("i", 100).build()));
            TableData tableData2 = awaitT(stats.findOne(new FindOptions.Builder().addWhere("player", uuid.toString()).build()));
            assert tableData2 != null;
            check(tableData2.getInteger("i") == 105, "Verify i is 105", "i was " + tableData2.getInteger("i"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void pass(String title) {
        check(true, title, null);
    }

    private static void fail(String title, String reason) {
        check(false, title, reason);
    }

    private static void check(boolean expression, String title, String errorMessage) {
        if (!expression) {
            System.out.println("FAIL " + title + " (reason: " + errorMessage + ")");
            System.exit(1);
        } else System.out.println("PASS " + title);
    }

    private static void initSQL() throws SQLException {
        Properties properties = new Properties();
        properties.put("autoReconnect", true);
        sequelize = new Sequelize("jdbc:sqlite::memory:");
        sequelize.authenticate(properties);
        tables.add("stats", stats = sequelize.define("stats", new TableDefinition[] {
                new TableDefinition.Builder("player", DataType.STRING).setPrimaryKey(true).setAllowNull(false).build(),
                new TableDefinition.Builder("booed", DataType.BOOLEAN).setDefaultValue(0).setAllowNull(true).build(),
                new TableDefinition.Builder("i", DataType.INTEGER).setAllowNull(true).build(),
        }));
        sequelize.sync();
    }
}
