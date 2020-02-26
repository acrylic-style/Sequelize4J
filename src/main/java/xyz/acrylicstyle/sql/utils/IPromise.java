package xyz.acrylicstyle.sql.utils;

import java.util.function.Supplier;

public interface IPromise<T> {
    T done(T resolve, Supplier<Throwable> reject);
}
