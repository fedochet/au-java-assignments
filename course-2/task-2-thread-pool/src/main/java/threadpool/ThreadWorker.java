package threadpool;

import java.util.ArrayDeque;
import java.util.Queue;

final class ThreadWorker implements Runnable {
    private final Queue<Runnable> taskQueue = new ArrayDeque<>();
    private final Object lock = new Object();

    @Override
    public void run() {
        while (true) {
            if (Thread.interrupted()) {
                return;
            }

            Runnable task;
            synchronized (lock) {
                while (taskQueue.isEmpty()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                task = taskQueue.remove();
            }

            task.run();
        }
    }

    public void addTask(Runnable task) {
        synchronized (lock) {
            taskQueue.add(task);
            lock.notify(); // notify one free worker
        }
    }
}
