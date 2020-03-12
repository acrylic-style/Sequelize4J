package xyz.acrylicstyle.sql;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Validate {
    public static void isTrue(boolean expression, String message) {
        if (!expression) throw new IllegalArgumentException(message);
    }

    public static void isTrue(boolean expression) {
        if (!expression) throw new IllegalArgumentException("Expression must be true");
    }

    @NotNull
    @Contract(value = "null, _ -> fail; !null, _ -> param1", pure = true)
    public static <T> T notNull(T t, String message) {
        if (t == null) throw new NullPointerException(message);
        return t;
    }
}
