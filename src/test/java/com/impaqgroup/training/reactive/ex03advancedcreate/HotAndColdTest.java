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

public class HotAndColdTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(HotAndColdTest.class);

    private AtomicInteger atomicInteger;

    @Before
    public void setUp(){
        this.atomicInteger = new AtomicInteger(0);
    }

    private Observable<String> createTemperatureObservable() {
        return Observable.create(emitter -> {
            LOGGER.info("Connecting to server, creating new thread");
            new Thread(() -> {//only example, do not try it at home (production code)
                Thread.currentThread().setName("Temperature reader client thread " + atomicInteger.incrementAndGet());
                LOGGER.info("Temperature client connected to temperature measurement server");
                Random random = new Random();
                try {
                    for (int i = 0; (i < 5) && (!emitter.isDisposed()); ++i) {
                        sleep(100);//simulate some network delay
                        emitter.onNext(String.format("Sample %d = %s", i, random.nextDouble() * 20));
                    }
                    emitter.onComplete();
                    LOGGER.info("Disconnected from server, thread will be stopped");
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
    public void shouldReadSomeMeasurements(){
        createTemperatureObservable()
                .skip(2)
                .take(1)
                .subscribe(new LoggableObserver<>());
        sleep(700);
    }

    @Test
    public void shouldReadMeasurementsTwice(){
        Observable<String> temperatureObservable = createTemperatureObservable();

        temperatureObservable.subscribe(new LoggableObserver<>());
        temperatureObservable.subscribe(new LoggableObserver<>());
        sleep(700);
    }

    @Test
    public void hotObservable(){
        ConnectableObservable<String> hotStream = createTemperatureObservable().publish();
        sleep(600);

        LOGGER.info("Before connecting, connection will be established in next line of code");
        hotStream.connect();
        LOGGER.info("Client connected to measurement server. We lost some of measurements");
        sleep(250);

        LOGGER.info("Let's subscribe to get remaining measurements");
        hotStream.subscribe(new LoggableObserver<>());
        hotStream.subscribe(new LoggableObserver<>());
        sleep(700);
    }

    @Test
    public void connectOnFirstClientDisconnectAfterLastClientDisconnect(){
        Observable<String> observable = createTemperatureObservable()
                .publish()
                .refCount();//the most important line
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

    @SneakyThrows
    private static void sleep(int milliseconds){
        TimeUnit.MILLISECONDS.sleep(milliseconds);
    }

}
