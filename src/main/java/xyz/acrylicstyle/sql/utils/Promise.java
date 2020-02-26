package xyz.acrylicstyle.sql.utils;

import java.util.function.Function;
/*
public class Promise<T> {
    private IPromise<T> promise;
    private T t;
    private Throwable throwable;

    public Promise(IPromise<T> promise) {
        this.promise = promise;
    }

    private void run() {
        promise.done(this::then)
    }

    private //

    public <U> Promise<U> then(Function<T, U> function) {
        try {
            if (throwable == null) return new Promise<U>((resolve, reject) -> {
                resolve(function.apply(t));
            });
            return new Promise<>();
        } catch (Throwable e) {
            return new Promise<>(null, e);
        }
    }

    public <U> Promise<U> catch_(Function<Throwable, U> function) {
        try {
            if (throwable != null) return new Promise<>(function.apply(throwable));
            return new Promise<>();
        } catch (Throwable e) {
            return new Promise<>(null, e);
        }
    }
}
*/