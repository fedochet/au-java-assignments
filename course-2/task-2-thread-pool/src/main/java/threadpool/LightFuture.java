package threadpool;

import java.util.function.Function;

interface LightFuture<T> {
    boolean isReady();
    T get() throws LightExecutionException;
    <R> LightFuture<R> thenApply(Function<? super T, ? extends R> function);
}
