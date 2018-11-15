package threadpool;

final class ThreadWorker implements Runnable {
    private final BlockingQueue<Task> queue;
    private boolean isStopped = false;

    ThreadWorker(BlockingQueue<Task> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (!isStopped) {
            try {
                Task task = queue.remove();
                task.run();
            } catch (InterruptedException ignored) {
                // If it is an actual stop, we will see `isStopped` set to true and will stop `while`,
                // because interrupting thread HB seeing interruption.
                //
                // This exception may raise in run() only when task is awaiting for other future to complete. So, if it
                // is thrown, then executor thread has been interrupted, and threadPool is shutting down. That's why
                // we can afford to lose task, taken from queue.
            }
        }
    }

    void stop() {
        isStopped = true;
    }

}
