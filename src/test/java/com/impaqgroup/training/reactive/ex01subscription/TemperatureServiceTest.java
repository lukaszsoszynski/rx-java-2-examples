package com.impaqgroup.training.reactive.ex01subscription;

import static com.impaqgroup.training.reactive.ex01subscription.TemperatureService.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TemperatureServiceTest {

    private TemperatureService temperatureService;

    @Before
    public void setUp(){
        this.temperatureService = new TemperatureService();
    }

    @Test
    public void shouldGetTemperature(){
        Observable<Double> doubleObservable = temperatureService.getTemperature(KALISZ);
        doubleObservable.subscribe(t -> log.info("Current temperature {}", t));
    }

    @Test
    public void shouldGetErrorCallback(){
        temperatureService
                .getTemperature(TemperatureService.BERLIN)
                .subscribe(t -> log.info("Current temperature {}", t),
                        ex -> log.error("Sth went wrong", ex));
    }

    @Test
    public void shouldGetCompletionEvent(){
        temperatureService
                .getTemperature("Pogubie Tylnie")
                .subscribe(t -> log.info("Current temperature {}", t),
                        ex -> log.error("Sth went wrong", ex),
                        () -> log.info("No more temperature data"));
    }

    @Test
    public void shouldGetTemperatureDataAndCompletionEvent(){
        temperatureService
                .getTemperature(LUBLIN)
                .subscribe(t -> log.info("Current temperature {}", t),
                        ex -> log.error("Sth went wrong", ex),
                        () -> log.info("No more temperature data"));
    }

    @Test
    public void observableShouldNotifyObserver(){
        temperatureService
                .getTemperature(ŁÓDŹ)
                .subscribe(new TemperatureObserver());
    }

    @Test
    public void shouldSubscribeButForWhat(){
        temperatureService
                .getTemperature(WARSZAWA)
                .subscribe();
    }

    @Test
    public void shouldNotReallyCancelSubscription(){
        Disposable subscription = temperatureService
                .getTemperature(POZNAŃ)
                .subscribe(t -> log.info("Current temperature {}", t));//synchronous observable<--
        subscription.dispose();//<-- cannot dispose subscription in this way in sync observable
    }

    @Test
    public void shouldCancelSubscription(){
        temperatureService
                .getTemperature(POZNAŃ)
                .subscribe(new BoredObserver());//<-- this subscriber will dispose subscription
    }

}

@Slf4j
class TemperatureObserver implements Observer<Double> {

    @Override
    public void onSubscribe(Disposable d) {
        log.info("I started subscribing temperature");
    }

    @Override
    public void onNext(Double t) {
        log.info("Current temperature {}", t);
    }

    @Override
    public void onError(Throwable ex) {
        log.error("Sth went wrong", ex);
    }

    @Override
    public void onComplete() {
        log.info("No more temperature data");
    }
}

@Slf4j
class BoredObserver implements Observer<Double>{

    private Optional<Disposable> disposableOptional = Optional.empty();

    @Override
    public void onSubscribe(Disposable subscription) {
        log.info("I got subscription. From now on I am abel to unsubscribe :)");
        this.disposableOptional = Optional.of(subscription);
    }

    @Override
    public void onNext(Double t) {
        disposableOptional.ifPresent(subscription -> subscription.dispose());
        log.info("I am not interested in temperature data like {}, give me sth more interesting!", t);
    }

    @Override
    public void onError(Throwable ex) {
        log.error("Sth went wrong", ex);
    }

    @Override
    public void onComplete() {
        log.info("No more temperature data");
    }
}