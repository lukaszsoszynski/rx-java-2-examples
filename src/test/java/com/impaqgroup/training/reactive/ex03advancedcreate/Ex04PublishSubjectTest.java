package com.impaqgroup.training.reactive.ex03advancedcreate;

import org.junit.Test;

import io.reactivex.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Ex04PublishSubjectTest {

    @Test
    public void notRecommendedWayOfCreatingObservable(){
        //observable created
        PublishSubject<String> subject = PublishSubject.create();

        //observer subscribes
        subject.subscribe(log::info, ex -> log.info("Error", ex), () -> log.info("Observable completed."));

        subject.onNext("First event emitted by subject.");
        subject.onNext("Last event emitted by subject.");
        subject.onComplete();
    }

    @Test
    public void howToLooseEvent(){
        PublishSubject<String> subject = PublishSubject.create();
        subject.onNext("This event never reach any subscriber!");

        log.info("Observer subscribe");
        subject.subscribe(log::info, ex -> log.info("Error", ex), () -> log.info("Observable completed."));

        subject.onComplete();
    }

    @Test
    public void howToLooseAllSubscribers(){
        PublishSubject<String> subject = PublishSubject.create();

        //two observer subscribe
        subject.subscribe(event -> log.info("Subscriber 1: " + event), ex -> log.info("Subscriber 1: Error occurred, unsubscribe."));
        subject.subscribe(event -> log.info("Subscriber 2: " + event), ex -> log.info("Subscriber 2: Error occurred, unsubscribe."));

        subject.onNext("This event reach two observers.");
        subject.onError(new RuntimeException("Error occurred, all subscribers will unsubscribe."));
        subject.onNext("This event never reach any observers!");
    }

}
