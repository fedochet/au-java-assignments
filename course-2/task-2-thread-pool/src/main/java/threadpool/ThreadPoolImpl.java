package threadpool;

import java.util.function.Supplier;

public class ThreadPoolImpl implements ThreadPool {

    private final Thread[] threads;
    private final BlockingQueue<Runnable> queue = new BlockingQueue<>();

    public static ThreadPoolImpl create(int threadsNumber) {
        ThreadPoolImpl threadPool = new ThreadPoolImpl(threadsNumber);
        threadPool.start();

        return threadPool;
    }

    private ThreadPoolImpl(int threadsNumber) {
        if (threadsNumber <= 0) {
            throw new IllegalArgumentException("Threads number must be positive, got " + threadsNumber);
        }

        threads = new Thread[threadsNumber];
        ThreadWorker threadWorker = new ThreadWorker(queue);

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

        queue.add(() -> {
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