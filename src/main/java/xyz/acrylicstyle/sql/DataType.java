package xyz.acrylicstyle.sql;

public enum DataType {
    STRING("VARCHAR(255)"),
    BIGINT("BIGINT(255)"),
    INT("INT(255)"),
    INTEGER("INT(255)"),
    SMALLINT("SMALLINT"),
    MEDIUMINT("MEDIUMINT"),
    TINYINT("TINYINT"),
    BOOL("BOOL"),
    BOOLEAN("BOOLEAN"),
    DECIMAL("DECIMAL"),
    DEC("DEC"),
    FLOAT("FLOAT"),
    DOUBLE("DOUBLE"),
    BIT("BIT"),
    TIME("TIME"),
    DATETIME("DATETIME"),
    TIMESTAMP("TIMESTAMP"),
    CHAR("CHAR"),
    BINARY("BINARY"),
    VARBINARY("VARBINARY"),
    BLOB("BLOB"),
    TEXT("TEXT"),
    ENUM("ENUM"),
    SET("SET"),
    DATE("DATE"),
    ;

    private String type;

    DataType(String type) {
        this.type = type;
    }

    public String getType() { return type; }
}
