package xyz.acrylicstyle.sql.utils;

public interface IPromise<T> {
    T done(T t);
}
