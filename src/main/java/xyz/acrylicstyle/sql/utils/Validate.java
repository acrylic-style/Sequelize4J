package xyz.acrylicstyle.sql.utils;

public class Validate {
    private Validate() {}

    public static <T> T notNull(T t) {
        notNull(t, "Object cannot be null");
        return t;
    }

    public static <T> T notNull(T t, String message) {
        if (t == null) throw new NullPointerException(message);
        return t;
    }

    public static void isTrue(boolean expression) {
        isTrue(expression, "Expression must be true");
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) throw new IllegalArgumentException(message);
    }
}
