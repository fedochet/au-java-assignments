package threadpool;

import java.util.function.Function;

class LightFutureImpl<T> implements LightFuture<T> {
    private final Object lock = new Object();

    private volatile boolean isReady = false;

    private boolean isSuccessful = false;
    private T result;
    private Throwable error;

    @Override
    public boolean isReady() {
        return isReady;
    }

    /**
     * If isReady == true, this thread can see isSuccessful and other fields, because
     * they were assigned before isReady was set.
     */
    @Override
    public T get() throws InterruptedException, LightExecutionException {
        if (!isReady) {
            synchronized (lock) {
                while (!isReady) { // spurious wakeup
                    lock.wait();
                }
            }
        }

        if (isSuccessful) {
            return result;
        } else {
            throw new LightExecutionException(error);
        }
    }

    @Override
    public <R> LightFuture<R> thenApply(Function<? super T, ? extends R> function) {
        throw new IllegalArgumentException("Not supported");
    }

    /**
     * isReady is assigned after setting other fields to allow HB in {@link LightFutureImpl#get()}.
     * <p>
     * Locking is required to prevent accidentally finishing future twice (might be removed because
     * in current code it might never happen).
     */
    public void finishWithResult(T result) {
        synchronized (lock) {
            assertNotFinished();

            isSuccessful = true;
            this.result = result;

            isReady = true;

            lock.notifyAll();
        }
    }

    public void finishWithError(Throwable error) {
        synchronized (lock) {
            assertNotFinished();

            isSuccessful = false;
            this.error = error;

            isReady = true;

            lock.notifyAll();
        }
    }

    private void assertNotFinished() {
        if (isReady) throw new IllegalStateException("This future is already finished!");
    }
}
