package xyz.acrylicstyle.sql;

import org.jetbrains.annotations.NotNull;
import util.collection.Collection;
import util.collection.CollectionList;

/**
 * @deprecated Draft API, may not work as expected.
 */
@Deprecated
class ArrayDataType<T> extends DataType<CollectionList<T>> {
    private Class<CollectionList<T>> clazz;

    @NotNull
    protected final DataType<T> arrayType;

    @NotNull
    private static final Collection<DataType<?>, ArrayDataType<?>> cache = new Collection<>();

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> ArrayDataType<T> arrayTypeOf(DataType<T> type) {
        if (cache.containsKey(type)) return (ArrayDataType<T>) cache.get(type);
        ArrayDataType<T> array = new ArrayDataType<>(type);
        cache.add(type, array);
        return array;
    }

    protected ArrayDataType(@NotNull DataType<T> arrayType) {
        super("ArrayDataType", null);
        this.arrayType = arrayType;
    }

    public Class<T> toComponentClass() { return arrayType.toClass(); }

    @SuppressWarnings({ "unchecked", "InstantiatingObjectToGetClassObject" })
    @Override
    public @NotNull Class<CollectionList<T>> toClass() {
        if (this.clazz != null) return this.clazz;
        this.clazz = (Class<CollectionList<T>>) new CollectionList<>().getClass(); // cache class
        return this.clazz;
    }

    @NotNull
    public DataType<T> getComponentType() { return arrayType; }

    @Override
    public String toString() {
        return "ArrayDataType{"
                + "arrayType=" + arrayType +
                ", type='" + type + '\'' +
                '}';
    }

    public static boolean isArrayDataType(DataType<?> type) { return type.getClass() == ArrayDataType.class; }
}
