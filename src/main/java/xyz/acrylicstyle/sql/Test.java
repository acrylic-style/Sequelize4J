package xyz.acrylicstyle.sql;

import util.CollectionList;
import util.StringCollection;
import xyz.acrylicstyle.sql.options.FindOptions;
import xyz.acrylicstyle.sql.options.InsertOptions;

import java.sql.SQLException;
import java.util.UUID;

import static util.promise.Promise.await;

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
            initSQLUsingMemory();
            try {
                await(stats.insert(new InsertOptions.Builder().addValue("player", uuid.toString()).addValue("booed", false).build()), null);
            } catch (Exception e) {
                fail("Insert into stats table", e.getMessage());
                e.printStackTrace();
                return;
            }
            pass("Insert into stats table");
            CollectionList<TableData> dataList = (CollectionList<TableData>) await(stats.findAll(new FindOptions.Builder().addWhere("player", uuid.toString()).build()), null);
            check(dataList.size() == 1, "Stats size", "Size must be 1");
            TableData tableData = dataList.first();
            check(tableData.get("player", String.class).equals(uuid.toString()),
                    "Validate player",
                    "player wasn't " + tableData.get("player", String.class) + " == " + uuid.toString());
            check(tableData.get("booed", int.class) == 0, "Verify booed state is false", "booed was true");
            CollectionList<TableData> dataList2;
            try {
                dataList2 = (CollectionList<TableData>) await(tableData.update("booed", 1, new FindOptions.Builder().addWhere("player", uuid.toString()).build()), null);
            } catch (Exception e) {
                e.printStackTrace();
                fail("Update booed state to true", e.getMessage());
                return;
            }
            pass("Update booed state to true");
            TableData data2 = dataList2.first();
            check(data2.get("booed", int.class) == 1, "Verify booed state is true", "booed was false");
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

    private static void initSQLUsingSQLite(String name) throws SQLException {
        initSQL("jdbc:sqlite:./plugins/Tosogame/" + name, null, null, null);
    }

    private static void initSQLUsingMemory() throws SQLException {
        initSQL("jdbc:sqlite::memory:", null, null, null);
    }

    private static void initSQL(String host, String database, String user, String password) throws SQLException {
        Sequelize.loadDriver();
        if (database == null || user == null || password == null) {
            sequelize = new Sequelize(host);
        } else {
            sequelize = new Sequelize(host, database, user, password);
        }
        sequelize.authenticate();
        tables.add("stats", stats = sequelize.define("stats", new TableDefinition[] {
                new TableDefinition.Builder("player", DataType.STRING).setPrimaryKey(true).setAllowNull(false).build(),
                new TableDefinition.Builder("booed", DataType.BOOLEAN).setDefaultValue(false).setAllowNull(true).build(),
        }));
        sequelize.sync();
    }
}
