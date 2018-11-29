package threadpool;

final class ThreadWorker implements java.lang.Runnable {
    private final BlockingQueue<Runnable> queue;
    private volatile boolean isStopped = false;

    ThreadWorker(BlockingQueue<Runnable> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (!isStopped) {
            try {
                Runnable task = queue.remove();
                task.run();
            } catch (InterruptedException ignored) {
                // If it is an actual stop, we will see `isStopped` set to true and will stop `while`,
                // because interrupting thread HB seeing interruption.
            }
        }
    }

    void stop() {
        isStopped = true;
    }

    boolean isStopped() {
        return isStopped;
    }
}
