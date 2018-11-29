package threadpool;

import java.util.function.Function;

public interface LightFuture<T> {
    boolean isReady();

    /**
     * @return result of execution.
     * @throws InterruptedException if caller thread was interrupted while waiting.
     * @throws LightExecutionException if computation throws some exception.
     */
    T get() throws InterruptedException, LightExecutionException;

    <R> LightFuture<R> thenApply(Function<? super T, ? extends R> function);
}
