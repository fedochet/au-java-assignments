package ru.spbau.task4;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Function2Test {
    @Test
    void function2_can_be_called() {
        Function2<String, String, String> concat = (i, j) -> i + j;

        assertThat(concat.apply("first", "second")).isEqualTo("firstsecond");
    }

    @Test
    void function2_can_be_composed_with_function1_with_exact_types() {
        Function2<String, String, String> concat = (i, j) -> i + j;
        Function1<String, String> reverse = s -> new StringBuilder(s).reverse().toString();

        Function2<String, String, String> concatAndReverse = concat.compose(reverse);

        assertThat(concatAndReverse.apply("abc", "def")).isEqualTo("fedcba");
    }

    @Test
    void first_argument_can_be_binded() {
        Function2<Integer, Integer, Double> pow = (a, b) -> Math.pow(a, b);

        Function1<Integer, Double> powOfTwo = pow.bind1(2);

        assertThat(powOfTwo.apply(10)).isEqualTo(Math.pow(2, 10));
    }

    @Test
    void second_argument_can_be_binded() {
        Function2<Integer, Integer, Double> pow = (a, b) -> Math.pow(a, b);

        Function1<Integer, Double> square = pow.bind2(2);

        assertThat(square.apply(10)).isEqualTo(Math.pow(10, 2));
    }

    @Test
    void function2_can_be_curried() {
        Function2<Integer, String, String> multString =
            (i, s) -> Stream.generate(() -> s).limit(i).collect(Collectors.joining());

        Function1<Integer, Function1<String, String>> curried = multString.curry();
        Function1<String, String> tripleString = curried.apply(3);

        assertThat(curried.apply(2).apply("str")).isEqualTo("strstr");
        assertThat(tripleString.apply("a")).isEqualTo("aaa");
        assertThat(tripleString.apply("b")).isEqualTo("bbb");
    }

    @Test
    @DisplayName("composition works with function that takes wider argument")
    void composition_works_with_wider_argument_type() {
        Function2<Integer, Integer, Integer> add = (i, j) -> i + j;
        Function1<Object, String> toString = Object::toString;

        Function2<Integer, Integer, String> addAndConvertToString = add.compose(toString);

        assertThat(addAndConvertToString.apply(1, 2)).isEqualTo("3");
    }

    @Test
    @DisplayName("composition works with function that returns more narrow type")
    void composition_works_with_narrower_return_type() {
        Function2<Integer, Integer, Integer> add = (i, j) -> i + j;
        Function1<Integer, String> intToString = Object::toString;

        Function2<Integer, Integer, Object> addAndConvertToObject = add.compose(intToString);

        assertThat(addAndConvertToObject.apply(1, 2)).isEqualTo("3");
    }

    @Test
    void compose_does_not_take_null() {
        Function2<Object, Object, Object> left = (i, j) -> i;

        assertThrows(NullPointerException.class, () -> {
            left.compose(null);
        });
    }
}