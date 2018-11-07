package threadpool;

final class ThreadWorker implements Runnable {
    private final BlockingQueue<Runnable> queue;
    private boolean isStopped = false;

    ThreadWorker(BlockingQueue<Runnable> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (!isStopped) {
            Runnable task;
            try {
                task = queue.remove();
            } catch (InterruptedException e) {
                // if it is an actual stop, we will see `isStopped` set to true and will stop `while`,
                // because interrupting thread HB seeing interruption
                // if it is a spurious wake up, we will try again
                continue;
            }

            task.run();
        }
    }

    public void stop() {
        isStopped = true;
    }
}
