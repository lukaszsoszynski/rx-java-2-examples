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
        //like Stream.of()
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
        Future<String> future = new SampleAlreadyCompleteFuture();//<-- support for future, but support for CompletableFuture is very poor
        Observable
                .fromFuture(future)
                .subscribe(loggableObserver);
    }

    @Test
    public void shouldCreateObservableFromRange(){
        //IntStream.range(0, 3) <-- like
        Observable
                .range(0, 3)
                .subscribe(new LoggableObserver<>());
    }

    @Test
    public void shouldCreateEmptyObservable(){
        Observable
                .<String>empty()
                .subscribe(loggableObserver);//<-- subscription & completion event
    }

    @Test
    public void shouldCreateNeverObservable(){
        Observable
                .<String>never()
                .subscribe(loggableObserver);//<-- it get only subscription
    }

    @Test
    public void shouldCreateObservableError(){
        Observable
                .<String>error(new RuntimeException("Catastrophic error"))
                .subscribe(loggableObserver);//<-- subscription & error
    }
}

class SampleAlreadyCompleteFuture implements Future<String> {

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