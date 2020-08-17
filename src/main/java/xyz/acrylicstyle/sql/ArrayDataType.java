package xyz.acrylicstyle.sql;

import org.jetbrains.annotations.NotNull;
import util.Collection;

/**
 * WIP, it does not work at all.
 */
class ArrayDataType<T> extends DataType<ArrayDataType<T>> {
    @NotNull
    protected final DataType<T> arrayType;

    @NotNull
    private static final Collection<DataType<?>, ArrayDataType<?>> cache = new Collection<>();

    @SuppressWarnings("unchecked")
    @NotNull
    /*public*/ static <T> ArrayDataType<T> arrayTypeOf(DataType<T> type) { // not implemented
        if (cache.containsKey(type)) return (ArrayDataType<T>) cache.get(type);
        ArrayDataType<T> array = new ArrayDataType<>(type);
        cache.add(type, array);
        return array;
    }

    protected ArrayDataType(@NotNull DataType<T> arrayType) {
        super("ArrayDataType", null);
        this.arrayType = arrayType;
    }

    @NotNull
    public DataType<T> getArrayType() { return arrayType; }

    @Override
    public String toString() {
        return "ArrayDataType{"
                + "arrayType=" + arrayType +
                ", type='" + type + '\'' +
                '}';
    }

    public static boolean isArrayDataType(DataType<?> type) { return type.getClass() == ArrayDataType.class; }
}
