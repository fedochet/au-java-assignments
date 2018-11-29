package threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class LightFutureImpl<T> implements LightFuture<T> {
    private final Object lock = new Object();
    private final BlockingQueue<Runnable> queue;
    private final List<Runnable> spawnedFuturesTasks = new ArrayList<>();

    private volatile State state = State.COMPUTING;

    private T result;
    private Throwable error;

    LightFutureImpl(BlockingQueue<Runnable> queue) {
        this.queue = queue;
    }

    @Override
    public boolean isReady() {
        return state != State.COMPUTING;
    }

    /**
     * If isReady == true, this thread can see isSuccessful and other fields, because
     * they were assigned before isReady was set.
     */
    @Override
    public T get() throws InterruptedException, LightExecutionException {
        if (!isReady()) {
            synchronized (lock) {
                while (!isReady()) { // spurious wakeup
                    lock.wait();
                }
            }
        }

        return getComputation();
    }

    @Override
    public <R> LightFuture<R> thenApply(Function<? super T, ? extends R> function) {
        LightFutureImpl<R> spawnedFuture = new LightFutureImpl<>(queue);

        Runnable spawnedFutureTask = () -> {
            try {
                R result = function.apply(getComputation());
                spawnedFuture.finishWithResult(result);
            } catch (RuntimeException e) {
                spawnedFuture.finishWithError(e);
            } catch (LightExecutionException e) {
                spawnedFuture.finishWithError(e.getCause());
            }
        };

        synchronized (spawnedFuturesTasks) {
            if (isReady()) {
                queue.add(spawnedFutureTask);
            } else {
                spawnedFuturesTasks.add(spawnedFutureTask);
            }
        }

        return spawnedFuture;
    }

    /**
     * isReady is assigned after setting other fields to allow HB in {@link LightFutureImpl#get()}.
     * <p>
     * Locking is required to prevent accidentally finishing future twice (might be removed because
     * in current code it might never happen).
     */
    void finishWithResult(T result) {
        synchronized (lock) {
            assertNotFinished();

            this.result = result;
            state = State.COMPUTED_SUCCESSFULLY;

            lock.notifyAll();
            submitSpawnedFutures();
        }
    }

    void finishWithError(Throwable error) {
        synchronized (lock) {
            assertNotFinished();

            this.error = error;
            state = State.COMPUTED_EXCEPTIONALLY;

            lock.notifyAll();
            submitSpawnedFutures();
        }
    }

    private void submitSpawnedFutures() {
        synchronized (spawnedFuturesTasks) {
            spawnedFuturesTasks.forEach(queue::add);
            spawnedFuturesTasks.clear(); // allowing gc to collect them if needed
        }
    }

    private T getComputation() throws LightExecutionException {
        if (state == State.COMPUTING) throw new IllegalStateException("Future is not computed!");

        if (state == State.COMPUTED_SUCCESSFULLY) {
            return result;
        } else {
            throw new LightExecutionException(error);
        }
    }

    private void assertNotFinished() {
        if (isReady()) throw new IllegalStateException("This future is already finished!");
    }

    private enum State {
        COMPUTING,
        COMPUTED_SUCCESSFULLY,
        COMPUTED_EXCEPTIONALLY
    }
}
