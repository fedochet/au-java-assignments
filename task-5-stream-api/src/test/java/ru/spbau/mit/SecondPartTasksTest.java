package ru.spbau.mit;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SecondPartTasksTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File aaa;
    private File bbb;

    @Before
    public void initFiles() throws IOException {
        aaa = folder.newFile();
        try (PrintWriter fileWriter = new PrintWriter(new FileWriter(aaa))) {
            fileWriter.println("aaastring1 aaastring2 aaastring3");
            fileWriter.println("aaastring4 aaastring5 aaastring6");
            fileWriter.println("aaastring7 aaastring8 aaastring9");
        }

        bbb = folder.newFile();
        try (PrintWriter fileWriter = new PrintWriter(new FileWriter(bbb))) {
            fileWriter.println("bbbstring1 bbbstring2 bbbstring3");
            fileWriter.println("bbbstring4 bbbstring5 bbbstring6");
            fileWriter.println("bbbstring7 bbbstring8 bbbstring9");
        }
    }

    @Test
    public void testFindQuotes() {
        assertEquals(
            Arrays.asList(
                "aaastring4 aaastring5 aaastring6",
                "bbbstring4 bbbstring5 bbbstring6"
            ),

            SecondPartTasks.findQuotes(
                Arrays.asList(aaa.getAbsolutePath(), bbb.getAbsolutePath()),
                "string5"
            )
        );
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