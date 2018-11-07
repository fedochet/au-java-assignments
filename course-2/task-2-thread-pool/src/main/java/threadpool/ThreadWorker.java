package threadpool;

final class ThreadWorker implements Runnable {
    private final BlockingQueue<Runnable> queue;

    ThreadWorker(BlockingQueue<Runnable> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            if (Thread.interrupted()) {
                break;
            }

            Runnable task;
            try {
                task = queue.remove();
            } catch (InterruptedException e) {
                break;
            }

            task.run();
        }
    }
}
