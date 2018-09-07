package ru.spbau.task4;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Function1Test {

    @Test
    void function_can_be_called() {
        Function1<Integer, Integer> square = i -> i * i;

        assertThat(square.apply(10)).isEqualTo(100);
    }

    @Test
    void composition_works_with_exact_types() {
        Function1<Integer, String> toString = i -> i.toString();
        Function1<String, Integer> length = String::length;

        Function1<String, String> lengthToString = length.compose(toString);

        assertThat(lengthToString.apply("hello world")).isEqualTo("11");
    }

    @Test
    @DisplayName("composition works with function that takes wider argument")
    void composition_works_for_wider_argument_type() {
        Function1<Object, Boolean> notNull = o -> o != null;
        Function1<String, String> stringId = s -> s;

        Function1<String, Boolean> notNullString = stringId.compose(notNull);

        assertThat(notNullString.apply("str")).isTrue();
        assertThat(notNullString.apply(null)).isFalse();
    }

    @Test
    @DisplayName("composition works with function that returns more narrow type")
    void composition_works_for_narrower_return_type() {
        Function1<Object, Object> id = i -> i;
        Function1<Object, String> toString = i -> i.toString();

        Function1<Object, Object> toStringObject = id.compose(toString);

        assertThat(toStringObject.apply(100)).isEqualTo("100");
    }

    @Test
    void compose_does_not_take_null() {
        Function1<Object, Object> id = i -> i;

        assertThrows(NullPointerException.class, () -> {
            id.compose(null);
        });
    }
}