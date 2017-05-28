package com.impaqgroup.training.reactive.other;

import static java.lang.System.out;

import org.junit.Test;

import io.reactivex.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PublishSubjectTest {

    @Test
    public void publishSubject(){
        PublishSubject<String> s = PublishSubject.create();

        s.subscribe(out::println);

        s.onNext("First event.");
        s.onNext("Last event.");

        s.onComplete();
    }

}
