package threadpool;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolImplTest {

    private final ThreadPool threadPool = ThreadPoolImpl.create(4);

    @AfterEach
    void shutdown() {
        threadPool.shutdown();
    }

    @Test
    void test_shutdown() {
        ThreadPoolImpl threadPool = ThreadPoolImpl.create(10);
        threadPool.shutdown();
    }

    @Test
    void test_simple_tasks() {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        List<LightFuture<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < 1024; i++) {
            futures.add(threadPool.submit(() ->
                returnAfterMillis(5, atomicInteger::incrementAndGet)
            ));
        }

        Set<Integer> values = futures.stream().map(this::safelyGet).collect(Collectors.toSet());
        Set<Integer> expected = Stream.iterate(1, i -> i + 1).limit(1024).collect(Collectors.toSet());

        assertEquals(1024, atomicInteger.get());
        assertEquals(expected, values);
    }

    @Test
    void test_light_exception_thrown() {
        LightFuture<String> error = threadPool.submit(() -> {
            sleepForMillis(50);
            throw new RuntimeException("Error");
        });

        assertThrows(LightExecutionException.class, error::get);
    }

    @Test
    void future_is_ready_after_exception_is_thrown() {
        LightFuture<String> error = threadPool.submit(() -> {
            sleepForMillis(50);
            throw new RuntimeException("Error");
        });

        assertThrows(LightExecutionException.class, error::get);
        assertTrue(error.isReady());
    }

    @Test
    void test_future_is_not_ready_after_creation() {
        LightFuture<String> future = threadPool.submit(() -> returnAfterMillis(100, "String"));

        assertTrue(!future.isReady());
    }

    @Test
    void future_is_ready_after_get_returns() throws LightExecutionException, InterruptedException {
        LightFuture<String> future = threadPool.submit(() -> returnAfterMillis(100, "result"));

        assertEquals("result", future.get());
        assertTrue(future.isReady());
    }

    @Test
    void there_is_actually_n_threads() throws LightExecutionException, InterruptedException {
        Set<Long> threadIds = Collections.synchronizedSet(new HashSet<>());
        ThreadPoolImpl threadPool = ThreadPoolImpl.create(100);

        List<LightFuture> futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            LightFuture<Boolean> submit = threadPool.submit(() -> {
                Long threadId = Thread.currentThread().getId();
                return threadIds.add(returnAfterMillis(50, threadId));
            });

            futures.add(submit);
        }

        for (LightFuture future : futures) {
            assertTrue((boolean) future.get());
        }

        assertEquals(100, threadIds.size());
    }

    @Test
    void thenApply_works_as_expected() throws LightExecutionException, InterruptedException {
        LightFuture<String> source = threadPool.submit(() -> returnAfterMillis(100, "result"));

        LightFuture<String> doubledSource = source.thenApply(r -> r + r);

        assertEquals("result" + "result", doubledSource.get());
    }

    @Test
    void count_pow_on_futures() throws LightExecutionException, InterruptedException {
        LightFuture<Integer> curr = threadPool.submit(() -> returnAfterMillis(10, 1));

        int n = 20;
        for (int i = 0; i < n; i++) {
            curr = curr.thenApply(j -> returnAfterMillis(1, j * 2));
        }

        assertEquals(Math.pow(2, n), (int) curr.get());
    }

    @Test
    void exception_from_derived_can_be_seen() throws InterruptedException {
        LightFuture<Integer> source = threadPool.submit(() -> returnAfterMillis(100, 1));

        LightFuture<Object> derived = source.thenApply(i -> {
            sleepForMillis(100);
            throw new RuntimeException("derived");
        });

        try {
            derived.get();
            fail("Derived should not return");
        } catch (LightExecutionException e) {
            assertEquals("derived", e.getCause().getMessage());
        }
    }

    @Test
    void exception_from_source_is_passed_to_derived_future() throws InterruptedException {
        LightFuture<Integer> source = threadPool.submit(() -> {
            sleepForMillis(100);
            throw new RuntimeException("source");
        });

        LightFuture<Integer> derived = source.thenApply(i -> i * 2);

        try {
            derived.get();
            fail("Derived should not return");
        } catch (LightExecutionException e) {
            assertEquals("source", e.getCause().getMessage());
        }
    }

    @Test
    void cannot_submit_into_shutdown_tread_pool() {
        ThreadPool threadPool = ThreadPoolImpl.create(10);
        threadPool.shutdown();

        assertThrows(IllegalStateException.class, () -> threadPool.submit(() -> 100));
    }

    @Test
    void thread_pool_does_not_gives_threads_to_thenApply_tasks_until_their_parent_is_ready() {
        assertTimeout(Duration.ofSeconds(6), () -> {
            LightFuture<Integer> parentTask = threadPool.submit(() -> returnAfterMillis(5000, 0));

            List<LightFuture<String>> children = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                children.add(parentTask.thenApply(j -> "children"));
            }

            // Those three tasks should start executing right away if `thenApply` doesn't get the thread.
            LightFuture<Integer> lf1 = threadPool.submit(() -> returnAfterMillis(5000, 1));
            LightFuture<Integer> lf2 = threadPool.submit(() -> returnAfterMillis(5000, 2));
            LightFuture<Integer> lf3 = threadPool.submit(() -> returnAfterMillis(5000, 3));

            assertEquals(1, (int)lf1.get());
            assertEquals(2, (int)lf2.get());
            assertEquals(3, (int)lf3.get());

            for (LightFuture<String> child : children) {
                assertEquals("children", child.get());
            }
        });
    }

    private <T> T safelyGet(LightFuture<T> f) {
        try {
            return f.get();
        } catch (InterruptedException | LightExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    private void sleepForMillis(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T returnAfterMillis(int millis, T t) {
        return returnAfterMillis(millis, () -> t);
    }

    private <T> T returnAfterMillis(int millis, Supplier<T> t) {
        sleepForMillis(millis);
        return t.get();
    }
}