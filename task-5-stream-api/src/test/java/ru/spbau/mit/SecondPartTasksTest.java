package ru.spbau.mit;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() {
        fail();
    }

    @Test
    public void testPiDividedBy4() {
        double areaOfCircle = Math.PI * Math.pow(0.5, 2);

        assertEquals(areaOfCircle, SecondPartTasks.piDividedBy4(), 0.01);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> authorsWithBooks = ImmutableMap.<String, List<String>>builder()
            .put("aaa", Arrays.asList("abc", "def", "g", "hi")) // 9
            .put("bbb", Arrays.asList("ab", "de", "f"))         // 5
            .put("ccc", Arrays.asList("abcd", "efgh"))          // 8
            .build();

        assertEquals("aaa", SecondPartTasks.findPrinter(authorsWithBooks).get());
    }

    @Test
    public void testCalculateGlobalOrder() {
        Map<String, Integer> map1 = ImmutableMap.<String, Integer>builder()
            .put("aaa", 1)
            .put("bbb", 2)
            .put("ccc", 3)
            .build();

        Map<String, Integer> map2 = ImmutableMap.<String, Integer>builder()
            .put("aaa", 4)
            .put("bbb", 5)
            .put("ddd", 6)
            .build();

        Map<String, Integer> map3 = ImmutableMap.<String, Integer>builder()
            .put("eee", 10)
            .put("fff", 11)
            .put("ggg", 12)
            .build();

        ImmutableMap<String, Integer> expected = ImmutableMap.<String, Integer>builder()
            .put("aaa", 5)
            .put("bbb", 7)
            .put("ccc", 3)
            .put("ddd", 6)
            .put("eee", 10)
            .put("fff", 11)
            .put("ggg", 12)
            .build();

        assertEquals(expected, SecondPartTasks.calculateGlobalOrder(Arrays.asList(map1, map2, map3)));
    }
}