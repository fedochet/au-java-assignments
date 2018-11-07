package threadpool;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Simple blocking queue to make interactions between threads easy.
 */
class BlockingQueue<T> {
    private final Object lock = new Object();
    private final Queue<T> taskQueue = new ArrayDeque<>();

    public void add(T task) {
        synchronized (lock) {
            taskQueue.add(task);
            lock.notify(); // notify one waiting person
        }
    }

    public T remove() throws InterruptedException {
        synchronized (lock) {
            while (taskQueue.isEmpty()) {
                lock.wait();
            }

            return taskQueue.remove();
        }
    }
}
