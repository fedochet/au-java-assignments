package threadpool;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ThreadPoolImplTest {

    private final ThreadPoolImpl threadPool = ThreadPoolImpl.create(4);

    @After
    public void shutdown() {
        threadPool.shutdown();
    }

    @Test
    public void test_shutdown() {
        ThreadPoolImpl threadPool = ThreadPoolImpl.create(10);
        threadPool.shutdown();
    }

    @Test
    public void test_simple_task() throws LightExecutionException, InterruptedException {
        List<LightFuture<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            final int j = i;
            LightFuture<Integer> result = threadPool.submit(() -> {
                sleepFor(4);
                return j;
            });

            futures.add(result);
        }

        for (LightFuture<Integer> future : futures) {
            System.out.println("Printing future!");
            System.out.println(future.get());
        }
    }

    @Test
    public void two_waiters_on_same_future() throws LightExecutionException, InterruptedException {
        LightFuture<String> task = threadPool.submit(() -> {
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Hello";
        });

        Supplier<Void> doubledTask = () -> {
            try {
                assertEquals("Hello", task.get());
            } catch (LightExecutionException | InterruptedException e) {
                fail(e.getMessage());
            }

            return null;
        };

        LightFuture<Void> waiter1 = threadPool.submit(doubledTask);
        LightFuture<Void> waiter2 = threadPool.submit(doubledTask);

        waiter1.get();
        waiter2.get();
    }

    private void sleepFor(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}