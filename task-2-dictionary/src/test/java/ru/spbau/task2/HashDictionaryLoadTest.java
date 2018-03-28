package ru.spbau.task2;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This set of test aim to check that {@link HashDictionary} works fine on big amount of data thrown at it.
 */
class HashDictionaryLoadTest {
    /**
     * Seed for reproducibility.
     */
    private static long SEED = 37;

    /**
     * For each test it is fresh to avoid depending on tests order.
     */
    private Random RANDOM = new Random(SEED);

    private Dictionary<Integer, Integer> dict = new HashDictionary<>();
    private HashMap<Integer, Integer> jdkDict = new HashMap<>();

    @Test
    void insertion_and_contains() {
        int bound = 1024;

        for (int i = 0; i < 1024; i++) {
            for (int j = 0; j < 1024; j++) {
                int key = RANDOM.nextInt(bound);
                int value = RANDOM.nextInt(bound);

                Integer dictPut = dict.put(key, value);
                Integer jdkDictPut = jdkDict.put(key, value);

                assertThat(dictPut).isEqualTo(jdkDictPut);
            }

            jdkDict.forEach((k, v) -> {
                assertThat(dict.contains(k)).isTrue();
                assertThat(dict.get(k)).isEqualTo(v);
            });

            assertThat(jdkDict).hasSize(dict.size());
        }
    }

    @Test
    void insertion_and_deletes() {
        int bound = 1024;

        for (int i = 0; i < 1024; i++) {
            for (int j = 0; j < 1024; j++) {
                if (j % 5 == 0) {
                    Integer key = getRandomElement(jdkDict.keySet());

                    Integer removed = dict.remove(key);
                    Integer jdkRemoved = jdkDict.remove(key);

                    assertThat(removed).isEqualTo(jdkRemoved);
                } else {
                    int key = RANDOM.nextInt(bound);
                    int value = RANDOM.nextInt(bound);

                    Integer dictPut = dict.put(key, value);
                    Integer jdkDictPut = jdkDict.put(key, value);

                    assertThat(dictPut).isEqualTo(jdkDictPut);
                }
            }

            jdkDict.forEach((k, v) -> {
                assertThat(dict.contains(k)).isTrue();
                assertThat(dict.get(k)).isEqualTo(v);
            });

            assertThat(jdkDict).hasSize(dict.size());
        }
    }

    @Nullable
    private Integer getRandomElement(Set<Integer> integers) {
        if (integers.isEmpty()) {
            return null;
        }

        return integers.stream()
            .skip(RANDOM.nextInt(integers.size()))
            .findFirst()
            .orElse(null);
    }
}
