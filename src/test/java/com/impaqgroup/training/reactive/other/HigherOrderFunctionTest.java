package com.impaqgroup.training.reactive.other;

import static java.lang.System.out;

import java.util.stream.Stream;

import org.junit.Test;

import lombok.Value;

public class HigherOrderFunctionTest {

    @Test
    public void shouldUseHigherOrder(){
        Stream.of(1, 2, 3)
            .map(n -> n + 1)
            .filter(n -> (n % 2) == 0)
            .forEach(out::println);
    }

    @Value
    public static class Immutable {
        private String name;
    }

}
