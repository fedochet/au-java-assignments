package threadpool;

/**
 * Runnable replacement with possible {@link InterruptedException} thrown.
 * It is done to make `thenApply`possible with transparent exception handling.
 */
interface Task {
    void run() throws InterruptedException;
}
