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

    @Test
    public void customFlowableWithoutBackpressureSupport(){
        Flowable<Double> flowable = Flowable.create(emitter -> {
            //this flowable emitter do not support backprassure and it cause errors
            for(int i = 0; i < 3; ++i){//always emits 3 event
                double random = Math.random();
                log.info("Next random number will be emitted {}", random);
                emitter.onNext(random);
            }
        }, BackpressureStrategy.ERROR);

        flowable
                .subscribeOn(Schedulers.io())
                .blockingSubscribe(new Subscriber<Double>() {

            Subscription s;

            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                log.info("Request only 1 event");
                s.request(1);
            }

            @Override
            public void onNext(Double randomNumber) {
                log.info("Subscriber get number {}", randomNumber);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error occurred, subscriber requested only one event but source emitted more!", t);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Test
    public void customFlowableWithPoorBackpressureSupport(){
        Flowable<Double> poorBackpresureSupport = Flowable.create(emitter -> {
            while (emitter.requested() > 0){//not a good implementation.
                //^^^ Handle correctly only request invocation from subscriber's onSubscribe
                //and emit only amount of event requested in onSubscribe. Invocation
                //of request method from onNext are ignored.
                double randomNumber = Math.random();
                log.info("Random number emitted {}", randomNumber);
                emitter.onNext(randomNumber);
            }
            log.info("No more event to emit.");
            emitter.onComplete();
        }, BackpressureStrategy.ERROR);

        poorBackpresureSupport
                .subscribeOn(Schedulers.io())
                .blockingSubscribe(new Subscriber<Double>(){

                    private Subscription s;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(3);//request 3 events
                    }

                    @Override
                    public void onNext(Double randomNumber) {
                        log.info("Random number {}", randomNumber);
                        sleep(1000);
                        s.request(3);//request more events after one sec.
                        //^ this request will be not fulfilled because end of
                        //while loop in Flowable.create
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Test
    public void customFlowableWithBackpressureSupport(){
        Flowable<Double> flowableWithBackpressureSupport = Flowable
                .generate(emitter -> emitter.onNext(Math.random()));
        //              ^^^ cannot call more then one time onNext from emitter in generate method

        flowableWithBackpressureSupport
                .subscribeOn(Schedulers.io())
                .blockingSubscribe(new Subscriber<Double>() {

                    public Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.subscription = s;
                        log.info("Request one element");
                        s.request(1);
                    }

                    @Override
                    public void onNext(Double randomNumber) {
                        log.info("Get random number {}", randomNumber);
                        if(randomNumber < 0.2){
                            log.info("Cancel subscription");
                            subscription.cancel();
                            return;
                        }
                        sleep(1000);
                        log.info("Request next one event");
                        subscription.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("Error here", t);
                    }

                    @Override
                    public void onComplete() {
                        log.info("Flowable completed.");
                    }
                });
    }

    @SneakyThrows
    private void sleep(long timeout){
        TimeUnit.MILLISECONDS.sleep(timeout);
    }
}
