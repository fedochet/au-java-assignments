package ru.spbau.task4;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.spbau.task4.FunctionUtils.createFunction1;
import static ru.spbau.task4.FunctionUtils.createFunction2;

class Function2Test {
    @Test
    void function2_can_be_called() {
        Function2<String, String, String> concat = createFunction2((i, j) -> i + j);

        assertThat(concat.apply("first", "second")).isEqualTo("firstsecond");
    }

    @Test
    void function2_can_be_composed_with_function1_with_exact_types() {
        Function2<String, String, String> concat = createFunction2((i, j) -> i + j);
        Function1<String, String> reverse = createFunction1(s -> new StringBuilder(s).reverse().toString());

        Function2<String, String, String> concatAndReverse = concat.compose(reverse);

        assertThat(concatAndReverse.apply("abc", "def")).isEqualTo("fedcba");
    }
}