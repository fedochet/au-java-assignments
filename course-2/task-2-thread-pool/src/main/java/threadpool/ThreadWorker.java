package threadpool;

final class ThreadWorker implements Runnable {
    private final BlockingQueue<Task> queue;
    private boolean isStopped = false;

    ThreadWorker(BlockingQueue<Task> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        Task task;
        while (!isStopped) {
            try {
                task = queue.remove();
            } catch (InterruptedException e) {
                // if it is an actual stop, we will see `isStopped` set to true and will stop `while`,
                // because interrupting thread HB seeing interruption
                // if it is a spurious wake up, we will try again
                continue;
            }

            executeTask(task);
        }
    }

    public void stop() {
        isStopped = true;
    }

    private void executeTask(Task task) {
        while (!isStopped) {
            try {

                task.run();
                return;

            } catch (InterruptedException ignored) {
            }
        }
    }
}
