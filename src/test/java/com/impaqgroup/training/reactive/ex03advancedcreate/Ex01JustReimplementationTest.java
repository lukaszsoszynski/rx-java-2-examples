package com.impaqgroup.training.reactive.ex03advancedcreate;

import org.junit.Test;

import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Ex01JustReimplementationTest {

    @Test
    public void reimplementObservableJust(){
        //Try to reimplement Observable.just()
        myJust("One", "Two", "Three").subscribe(e -> log.info("Observer get event '{}'.", e));
    }

    // Very important method
    private <T> Observable<T> myJust(T...values){
        //feel free to call create method in RxJava 2
        return Observable.create(emitter -> {
            //this callback runs on each subscription
            log.info("On subscriber callback called!");
            //emitter <-- via this object you can pump event into stream
            for(T event : values){//<-- legacy for loop
                log.info("Pump event '{}' into observable stream.", event);
                emitter.onNext(event); //<-- The most important line
            }
            log.info("Before completion event");
            emitter.onComplete();//<-- notify all subscribers, that no more event left
        });
    }

    @Test
    public void withoutSubscription(){
        Observable<String> observableWithoutObservers = myJust("One", "Two");
        //pay attention to log, what can you see? Is it lazy?
    }

    @Test
    public void observableWithTwoSubscribers(){
        log.info("1) Observable created");
        Observable<String> observable = myJust("One", "Two", "Three");

        log.info("2) First observer subscribe");
        observable.subscribe(event -> log.info("First observer get '{}'", event));

        log.info("3) Second observer subscribe");
        observable.subscribe(event -> log.info("Second observer get '{}'", event));
        //how many times OnSubscribe callback was invoked? (find log "On subscriber callback called!")
    }

    @Test
    public void subscribeWithoutSubscriber(){
        Observable<String> observable = myJust("One", "Two", "Three");
        observable.subscribe();
        //how many times OnSubscribe callback was invoked? (find log "On subscriber callback called!")
    }

    @Test
    public void errorHandlingInOnSubscribe(){
        Observable.<Integer>create(emitter ->{
            try {
                for (int i = -5; i < 6; i++) {
                    emitter.onNext(100 / i);//<-- Exception here when i == 0
                }
                emitter.onComplete();
            }catch (Exception ex){
                emitter.onError(ex); //<-- error handling in OnSubscribe callback is mandatory!
            }
        }).subscribe(event -> log.info("Get event '{}'", event),
                ex -> log.error("Error callback.", ex),
                ()-> log.info("No more events."));
    }
}
