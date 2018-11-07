package threadpool;

import java.util.function.Supplier;

public class ThreadPoolImpl implements ThreadPool {

    private final Thread[] threads;
    private final ThreadWorker threadWorker = new ThreadWorker();

    public static ThreadPoolImpl create(int threadsNumber) {
        ThreadPoolImpl threadPool = new ThreadPoolImpl(threadsNumber);
        threadPool.start();

        return threadPool;
    }

    private ThreadPoolImpl(int threadsNumber) {
        threads = new Thread[threadsNumber];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(threadWorker);
        }
    }

    private void start() {
        for (Thread thread : threads) {
            thread.start();
        }
    }

    @Override
    public <T> LightFuture<T> submit(Supplier<? extends T> task) {
        LightFutureImpl<T> result = new LightFutureImpl<>();

        threadWorker.addTask(() -> {
            try {
                result.finishWithResult(task.get());
            } catch (Throwable e) {
                result.finishWithError(e);
            }
        });

        return result;
    }

    @Override
    public void shutdown() {
        for (Thread thread : threads) {
            thread.interrupt();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {

            }
        }
    }

}