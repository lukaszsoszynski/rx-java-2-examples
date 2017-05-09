package com.impaqgroup.training.reactive.ex08backpressure;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BackPressureTest {

    @Test
    public void shouldRespectBackPressure(){

        Subscriber<Integer> subscriber = new Subscriber<Integer>(){
            //^^^ use Subscriber instead of io.reactivex.Observer to gain back pressure support

            @Override
            public void onSubscribe(Subscription s) {//<-- Observer receive here Disposable
                s.request(3);//<-- we ask to get only 3 events
            }

            @Override
            public void onNext(Integer o) {
                log.info("{}", o);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Oops, something is missing?", t);
            }

            @Override
            public void onComplete() {
                log.info("Complete :)");
            }
        };

        Flowable
                .range(0, 100)
                .subscribe(subscriber);

    }

    @Test
    public void shouldRespectBackPressure2(){

        Subscriber<Integer> subscriber = new Subscriber<Integer>() {

            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                s.request(3);//<-- ask for 3 events
            }

            @Override
            public void onNext(Integer integer) {
                log.info("{}", integer);//try to guess max printed number
                if(integer < 50) {
                    subscription.request(1); //<--ask for one event
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Oops, something is missing?", t);
            }

            @Override
            public void onComplete() {
                log.info("Complete :)");
            }
        };


        Flowable.range(1, 100)
                .subscribe(subscriber);
    }

    @Test
    public void withoutBackPressure(){
        Observable
                .range(1, 100)//<-- create Observable _without_ back presser support
                .toFlowable(BackpressureStrategy.ERROR)//<-- explicit conversion to type with back pressure
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(3);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        log.info("{}", integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("Oops, something is missing?", t);
                    }

                    @Override
                    public void onComplete() {
                        log.info("Complete :)");
                    }
                });
    }

    @Test
    public void shouldBufferEventsOnBackPressure(){
        Observable
                .range(1, 100) //<-- type without back pressure support
                .toFlowable(BackpressureStrategy.BUFFER)
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(5);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        log.info("{}", integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("Oops, something is missing?", t);
                    }

                    @Override
                    public void onComplete() {
                        log.info("Complete :)");
                    }
                });
    }

    @Test
    public void shouldDropEventsOnBackPressure(){
        Observable
                .range(1, 100) //<-- type without back pressure support
                .toFlowable(BackpressureStrategy.DROP)
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(5);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        log.info("{}", integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("Oops, something is missing?", t);
                    }

                    @Override
                    public void onComplete() {
                        log.info("Complete :)");//<-- this is printed
                    }
                });
    }

    @Test
    public void shouldGetLatestEventsOnBackPressure(){
        Observable
                .range(1, 100) //<-- type without back pressure support
                .toFlowable(BackpressureStrategy.LATEST)
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(5);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        log.info("{}", integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("Oops, something is missing?", t);
                    }

                    @Override
                    public void onComplete() {
                        log.info("Complete :)");
                    }
                });
    }

    @Test
    public void shouldMissingEventsOnBackPressure(){
        Observable
                .range(1, 100) //<-- type without back pressure support
                .toFlowable(BackpressureStrategy.MISSING)//<-- now downstream have to deal with many events, back pressure is ignored :o
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(1);//<-- ask only for one event
                    }

                    @Override
                    public void onNext(Integer integer) {
                        log.info("{}", integer);//how many event does it get?
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("Oops, something is missing?", t);
                    }

                    @Override
                    public void onComplete() {
                        log.info("Complete :)");
                    }
                });
    }

    @Test
    public void shouldMissingAndDropEventsOnBackPressure(){
        Observable
                .range(1, 100) //<-- type without back pressure support
                .toFlowable(BackpressureStrategy.MISSING)//<-- now downstream have to deal with many events
                .onBackpressureDrop()//<-- now downstream drops events on back pressure (see other method onBackpressureXXX)
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(1);//<-- ask only for one event
                    }

                    @Override
                    public void onNext(Integer integer) {
                        log.info("{}", integer);//how many event does it get?
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("Oops, something is missing?", t);
                    }

                    @Override
                    public void onComplete() {
                        log.info("Complete :)");
                    }
                });
    }

    @Test
    public void twoSubscribersWithAndWithoutBackPressure() throws InterruptedException {
        Flowable<Integer> flowable = Flowable.range(1, 10)
                .publish()//<-- subject is created! (ConnectableFlowable)
                .refCount()//<-- lets connect on first subscriber
                .subscribeOn(Schedulers.io());//<-- and run it on another thread

        flowable.subscribe(new Subscriber<Integer>() {

            private Subscription subscription;
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
                this.subscription = s;
            }

            @Override
            public void onNext(Integer integer) {
                log.info("With back pressure {}, wait 250 ms for next event", integer);
                sleep(250);
                subscription.request(1);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Oops, id something missing?", t);
            }

            @Override
            public void onComplete() {
                log.info("Complete :)");
            }
        });

        flowable.subscribe(integer -> log.info("Subscriber _without_ back pressure, {}", integer));
        //^^^ another subscriber

        sleep(3000);
    }

    @SneakyThrows
    private void sleep(long timeout){
        TimeUnit.MILLISECONDS.sleep(timeout);
    }
}
