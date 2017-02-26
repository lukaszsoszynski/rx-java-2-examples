package com.impaqgroup.training.reactive.ex02creatingobservable;

import java.util.Arrays;
import java.util.concurrent.*;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Observable;

public class LoggableObserverTest {

    private LoggableObserver<String> loggableObserver;

    @Before
    public void setUp(){
        this.loggableObserver = new LoggableObserver<>();
    }

    @Test
    public void shouldJustCreateObservable(){
        Observable
                .just("Ala", "has", "got", "a", "cat")
                .subscribe(loggableObserver);
    }

    @Test
    public void shouldCreateObservableFromIterable(){
        Iterable<String> iterable = Arrays.asList("Ala", "has", "got", "a", "cat");
        Observable
                .fromIterable(iterable)
                .subscribe(loggableObserver);
    }

    @Test
    public void shouldCreateObservableFromFuture(){
        Future<String> future = new CompleteFuture();
        Observable
                .fromFuture(future)
                .subscribe(loggableObserver);
    }

    @Test
    public void shouldCreateObservableFromRange(){
        Observable
                .range(0, 3)
                .subscribe(new LoggableObserver<>());
    }

    @Test
    public void shouldCreateEmptyObservable(){
        Observable
                .<String>empty()
                .subscribe(loggableObserver);
    }

    @Test
    public void shouldCreateNeverObservable(){
        Observable
                .<String>never()
                .subscribe(loggableObserver);
    }

    @Test
    public void shouldCreateObservableError(){
        Observable
                .<String>error(new RuntimeException("Catastrophic error"))
                .subscribe(loggableObserver);
    }
}

class CompleteFuture implements Future<String> {

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public String get() throws InterruptedException, ExecutionException {
        return "Ala has got a cat";
    }

    @Override
    public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return get();
    }
}