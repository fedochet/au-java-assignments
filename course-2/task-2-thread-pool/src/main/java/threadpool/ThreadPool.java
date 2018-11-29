package threadpool;

import java.util.function.Supplier;

public interface ThreadPool {
    <T> LightFuture<T> submit(Supplier<? extends T> task);
    void shutdown();
}
