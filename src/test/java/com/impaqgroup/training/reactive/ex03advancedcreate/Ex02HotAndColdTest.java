package com.impaqgroup.training.reactive.ex03advancedcreate;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impaqgroup.training.reactive.ex02creatingobservable.LoggableObserver;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import lombok.SneakyThrows;

public class Ex02HotAndColdTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(Ex02HotAndColdTest.class);

    private AtomicInteger atomicInteger;

    @Before
    public void setUp(){
        this.atomicInteger = new AtomicInteger(0);
    }

    private Observable<String> createTemperatureObservable() {
        //Real thread is used.
        return Observable.create(emitter -> {
            LOGGER.info("Connecting to server, creating new thread");
            emitter.setCancellable(() -> LOGGER.info("Subscription canceled callback"));//<-- this callback maybe useful sometimes
            new Thread(() -> {//only example, do not try it at home (production code)
                Thread.currentThread().setName("Temperature reader client thread " + atomicInteger.incrementAndGet());//<-- each time different thread name
                LOGGER.info("Temperature client connected to temperature measurement server");
                Random random = new Random();
                int i = 0;
                try {
                    for (; (i < 5) && (!emitter.isDisposed()); ++i) {//<--isDisposed() check here
                        sleep(100);//simulate some network delay
                        emitter.onNext(String.format("Sample %d = %s", i, random.nextDouble() * 20));
                    }
                    emitter.onComplete();
                    LOGGER.info("Disconnected from server, thread will be stopped, events emitted {}", i);
                } catch (Exception ex) {
                    emitter.onError(ex);
                }
            }).start();
        });
    }

    @Test
    public void shouldReadAllMeasurements(){
        Observable<String> observable = createTemperatureObservable();
        observable.subscribe(new LoggableObserver<>());
        sleep(700);
    }

    @Test
    public void shouldReadMeasurementsTwice(){
        Observable<String> temperatureObservable = createTemperatureObservable();

        //two thread started here, please pay attention to thread name in the log
        temperatureObservable.subscribe(new LoggableObserver<>());
        temperatureObservable.subscribe(new LoggableObserver<>());
        sleep(700);
    }

    @Test
    public void hotObservable(){
        ConnectableObservable<String> hotStream = createTemperatureObservable().publish();//~PublishSubject created
        sleep(600);

        LOGGER.info("Before connecting, connection will be established in next line of code");
        hotStream.connect();//<--- most important line
        LOGGER.info("Client NOT connected to measurement server. We lost some of measurements");
        sleep(250);

        LOGGER.info("Let's subscribe to get remaining measurements");
        hotStream.subscribe(new LoggableObserver<>());
        hotStream.subscribe(new LoggableObserver<>());
        //how many thread was started?
        sleep(700);
    }

    @Test
    public void connectOnFirstClientDisconnectAfterLastClientDisconnect(){
        Observable<String> observable = createTemperatureObservable()
                .publish()
                .refCount();//<-- the most important line
        sleep(200);

        LOGGER.info("Before connecting, connection will be established in next line of code");
        Disposable firstSubscriber = observable.subscribe(measurement -> LOGGER.info("Client 1: measurement: {}", measurement));
        sleep(150);

        LOGGER.info("Second client connected");
        Disposable secondSubscriber = observable.subscribe(measurement -> LOGGER.info("Client 2: measurement: {}", measurement));
        sleep(150);

        firstSubscriber.dispose();
        LOGGER.info("Client 1 disconnected");

        sleep(150);

        LOGGER.info("Last client will be disposed so that connection to measurement server will be closed");
        secondSubscriber.dispose();
        sleep(200);
    }

    @Test
    public void fastDisconnectEventLost(){
        Observable<String> observable = createTemperatureObservable()
                .publish()
                .refCount();//<--
        sleep(200);

        LOGGER.info("Before connecting, connection will be established in next line of code");
        Disposable firstSubscriber = observable.subscribe(measurement -> LOGGER.info("Client 1: measurement: {}", measurement));
        sleep(200);
        Disposable secondSubscriber = observable.subscribe(measurement -> LOGGER.info("Client 2: measurement: {}", measurement));
        sleep(150);

        LOGGER.info("Subscribers will be disconnected...");
        firstSubscriber.dispose();
        secondSubscriber.dispose();
        LOGGER.info("Both subscriber disconnected.");
        //Now thread will be stopped, how many event was emitted?

        sleep(2000);
    }

    //-------- Async to Sync bridge

    @Test
    public void isSleepAtTheEndOfTestReallyNeeded(){
        Observable<String> observable = createTemperatureObservable();

        observable.subscribe(event -> LOGGER.info("How many times this text will be printed? ({})", event));
    }

    @Test
    public void betterWayToWaitForTestTermination(){
        Observable<String> observable = createTemperatureObservable();

        observable.blockingSubscribe(event -> LOGGER.info("Blocking subscriber get event '{}'", event));
        //^ please pay attention in which thread the above callback is invoked
    }

    @Test
    public void thereIsPlentyOfBlockingMethod(){
        Observable<String> observable = createTemperatureObservable();

        Iterable<String> iterable = observable.blockingIterable();
        for(String s : iterable){
            LOGGER.info("Another blocking method, get event '{}'.", s);
        }

        LOGGER.info("Blocking first method invoked: '{}'", observable.blockingFirst());
    }

    @SneakyThrows
    private static void sleep(int milliseconds){
        TimeUnit.MILLISECONDS.sleep(milliseconds);
    }

}
