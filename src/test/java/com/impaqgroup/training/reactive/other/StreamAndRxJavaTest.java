package com.impaqgroup.training.reactive.other;

import static java.lang.Math.pow;

import java.util.stream.Stream;

import org.junit.Test;

import io.reactivex.Observable;

public class StreamAndRxJavaTest {

    @Test
    public void stream(){
        Stream.of(1, 2, 3)
            .map(number -> pow(number, 2))
            .filter(number -> number > 2)
            .forEach(System.out::println);
    }

    @Test
    public void observable(){
        Observable.just(1, 2, 3)
            .map(number -> pow(number, 2))
            .filter(number -> number > 2)
            .forEach(System.out::println);

    }

}
