package com.impaqgroup.training.reactive.ex04flatmap;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Ex00TrivialMapExample {

    @Test
    public void shouldUseMapOnOptional(){
        String message = Optional.of(Math.PI) //<-- Optional(Math.PI)
            .map(number -> String.format("%1.2f", number)) //<-- Optional("3.14")
            .map(number -> "Pi value is: " + number) //<-- Optional("Pi value is: 3.14")
            .map(String::toUpperCase)//<-- Optional("PI VALUE IS: 3.14")
            .get();
      log.info("Message: {}", message);
    }

    @Test
    public void shouldUseMapOnStream(){
        Arrays.asList(1, 2, 3)
            .stream()
            .map(number -> number * 2)//    Stream(2, 4, 6)
            .map(Integer::toBinaryString)// Stream("10", "100", "110")
            .map(Integer::parseInt)//       Stream(10, 100, 110)
            .map(number -> number + 1)//    Stream(11, 101, 111)
            .forEach(number -> log.info("Number: {}", number));
    }

}
