package xyz.acrylicstyle.sql.exceptions;

import xyz.acrylicstyle.sql.DataType;

public class IncompatibleTypeException extends RuntimeException {
    private DataType dataType;
    private Class<?> clazz;

    public IncompatibleTypeException(DataType type, Class<?> clazz) {
        this.dataType = type;
        this.clazz = clazz;
    }

    @Override
    public String getMessage() {
        return "Incompatible type conversation: SQL Data Type " + dataType.name() + " (" + dataType.getType() + ") is incompatible with class: " + clazz.getCanonicalName();
    }
}
