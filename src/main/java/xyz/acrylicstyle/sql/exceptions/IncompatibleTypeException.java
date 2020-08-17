package xyz.acrylicstyle.sql.exceptions;

import xyz.acrylicstyle.sql.DataType;

public class IncompatibleTypeException extends RuntimeException {
    private final DataType<?> dataType;
    private final Class<?> clazz1;
    private final Class<?> clazz2;

    public IncompatibleTypeException(DataType<?> type, Class<?> clazz) {
        this(type, clazz, null);
    }

    public IncompatibleTypeException(DataType<?> type, Class<?> clazz1, Class<?> clazz2) {
        this.dataType = type;
        this.clazz1 = clazz1;
        this.clazz2 = clazz2;
    }

    @Override
    public String getMessage() {
        if (clazz2 != null)
            return "Incompatible type conversation: SQL Data Type " + dataType.name() + " (Type: '" + dataType.getType() + "') (Class: " + clazz2.getCanonicalName() + ") is incompatible with class: " + clazz1.getCanonicalName();
        return "Incompatible type conversation: SQL Data Type " + dataType.name() + " (Type: '" + dataType.getType() + "') is incompatible with class: " + clazz1.getCanonicalName();
    }
}
