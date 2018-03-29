package ru.spbau.task2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This set of test aim to check that {@link HashDictionary} works fine on big amount of data thrown at it.
 *
 * Tests here are extremely ugly.
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

    @Test
    void insertions_and_deletes_on_bad_objects() {
        int badObjectsHashesNumber = 32;

        Dictionary<Object, Integer> dict = new HashDictionary<>();
        HashMap<Object, Integer> jdkDict = new HashMap<>();

        for (int i = 0; i < 128; i++) {
            System.out.println("Iteration " + i);
            for (int j = 0; j < 128; j++) {
                if (j % 5 == 0) {
                    Object key = getRandomElement(jdkDict.keySet());

                    Integer removed = dict.remove(key);
                    Integer jdkRemoved = jdkDict.remove(key);

                    assertThat(removed).isEqualTo(jdkRemoved);
                } else {
                    Object key = createBadObject(badObjectsHashesNumber);
                    int value = RANDOM.nextInt();

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
    private <T> T getRandomElement(Set<T> integers) {
        if (integers.isEmpty()) {
            return null;
        }

        return integers.stream()
            .skip(RANDOM.nextInt(integers.size()))
            .findFirst()
            .orElse(null);
    }

    @NotNull
    private Object createBadObject(int bound) {
        int hashCode = RANDOM.nextInt(bound);
        return new Object() {
            @Override
            public int hashCode() {
                return hashCode;
            }
        };
    }

}
