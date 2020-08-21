package xyz.acrylicstyle.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class DataType<T> {
    public static final DataType<String> STRING = new DataType<>("STRING", "VARCHAR(255)"); // => getString
    public static final DataType<Long> BIGINT = new DataType<>("BIGINT", "BIGINT(255)"); // => getLong
    public static final DataType<Integer> INT = new DataType<>("INT", "INT(255)"); // => getInt
    public static final DataType<Integer> INTEGER = new DataType<>("INTEGER", "INT(255)");
    public static final DataType<Short> SMALLINT = new DataType<>("SMALLINT", "SMALLINT(255)");
    public static final DataType<Integer> MEDIUMINT = new DataType<>("MEDIUMINT", "MEDIUMINT(255)");
    public static final DataType<Byte> TINYINT = new DataType<>("TINYINT", "TINYINT(255)");
    public static final DataType<Boolean> BOOL = new DataType<>("BOOL", "BOOLEAN");
    public static final DataType<Boolean> BOOLEAN = new DataType<>("BOOLEAN", "BOOLEAN");
    public static final DataType<BigDecimal> DECIMAL = new DataType<>("DECIMAL", "DECIMAL");
    public static final DataType<BigDecimal> DEC = new DataType<>("DEC", "DEC");
    public static final DataType<Float> FLOAT = new DataType<>("FLOAT", "FLOAT");
    public static final DataType<Double> DOUBLE = new DataType<>("DOUBLE", "DOUBLE");
    public static final DataType<Boolean> BIT = new DataType<>("BIT", "BIT");
    public static final DataType<Time> TIME = new DataType<>("TIME", "TIME");
    public static final DataType<Timestamp> DATETIME = new DataType<>("DATETIME", "DATETIME");
    public static final DataType<Timestamp> TIMESTAMP = new DataType<>("TIMESTAMP", "TIMESTAMP");
    public static final DataType<String> CHAR = new DataType<>("CHAR", "CHAR");
    public static final DataType<Byte[]> BINARY = new DataType<>("BINARY", "BINARY");
    public static final DataType<Byte[]> VARBINARY = new DataType<>("VARBINARY", "VARBINARY");
    public static final DataType<Byte[]> BLOB = new DataType<>("BLOB", "BLOB(65535)");
    public static final DataType<String> TEXT = new DataType<>("TEXT", "TEXT(65535)");
    public static final DataType<Enum<?>> ENUM = new DataType<>("ENUM", "ENUM");
    public static final DataType<Enum<?>> SET = new DataType<>("SET", "SET");
    public static final DataType<Date> DATE = new DataType<>("DATE", "DATE");

    @Nullable
    protected final String type;

    @NotNull
    protected final String name;

    @NotNull
    public String name() { return name; }

    protected DataType(@NotNull String name, @Nullable String type) {
        this.name = name;
        this.type = type;
    }

    @Nullable
    public String getType() { return type; }

    public boolean isSpecialType() { return this.getClass() != DataType.class; }

    @SuppressWarnings("unchecked")
    @NotNull
    public Class<T> toClass() {
        if (TINYINT.equals(this)) {
            return (Class<T>) Byte.class;
        } else if (SMALLINT.equals(this)) {
            return (Class<T>) Short.class;
        } else if (INTEGER.equals(this) || INT.equals(this) || MEDIUMINT.equals(this)) {
            return (Class<T>) Integer.class;
        } else if (BIGINT.equals(this)) {
            return (Class<T>) Long.class;
        } else if (FLOAT.equals(this)) {
            return (Class<T>) Float.class;
        } else if (DECIMAL.equals(this) || DEC.equals(this)) {
            return (Class<T>) BigDecimal.class;
        } else if (CHAR.equals(this) || STRING.equals(this) || TEXT.equals(this)) {
            return (Class<T>) String.class;
        } else if (BOOL.equals(this) || BOOLEAN.equals(this) || BIT.equals(this)) {
            return (Class<T>) Boolean.class;
        } else if (BINARY.equals(this) || VARBINARY.equals(this) || BLOB.equals(this)) {
            return (Class<T>) Byte[].class;
        } else if (DATETIME.equals(this) || TIMESTAMP.equals(this)) {
            return (Class<T>) Timestamp.class;
        } else if (DATE.equals(this)) {
            return (Class<T>) Date.class;
        } else if (TIME.equals(this)) {
            return (Class<T>) Time.class;
        } else if (ENUM.equals(this) || SET.equals(this)) {
            return (Class<T>) Enum.class;
        } else if (DOUBLE.equals(this)) {
            return (Class<T>) Double.class;
        }
        throw new RuntimeException(this + " has missing mapping!");
    }

    @Override
    public String toString() {
        return "DataType{type='" + this.type + "'}";
    }
}
