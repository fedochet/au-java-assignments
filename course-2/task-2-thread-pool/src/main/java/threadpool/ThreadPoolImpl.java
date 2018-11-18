package threadpool;

import java.util.function.Supplier;

// TODO: 08.11.18 throw if tasks are submitted into shutdown pool
// TODO: 08.11.18 make all classes public
public final class ThreadPoolImpl implements ThreadPool {

    private final Thread[] threads;
    private final BlockingQueue<Task> queue = new BlockingQueue<>();
    private final ThreadWorker threadWorker = new ThreadWorker(queue);

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
        if (threadWorker.isStopped()) {
            throw new IllegalStateException("Cannot submit, threadpool has been shut down");
        }

        LightFutureImpl<T> result = new LightFutureImpl<>(queue);

        queue.add(() -> {
            try {
                result.finishWithResult(task.get());
            } catch (RuntimeException e) {
                result.finishWithError(e);
            }
        });

        return result;
    }

    /**
     * We shut down thread pool by stopping worker, interrupting all threads, and then
     * joining them all, ignoring any {@link InterruptedException}.
     *
     * We have to call {@link ThreadWorker#stop()} before interrupting threads, because
     * only that way we will see {@link ThreadWorker#isStopped} flag set to true inside of worker loop.
     */
    @Override
    public void shutdown() {
        threadWorker.stop();

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