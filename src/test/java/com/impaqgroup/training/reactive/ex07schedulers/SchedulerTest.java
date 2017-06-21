package com.impaqgroup.training.reactive.ex07schedulers;

import java.util.concurrent.*;

import org.junit.Test;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchedulerTest {

    @Test
    public void withoutSchedulers(){
        Observable<Long> observable = createSlowObservable();//<-- explain how slow observable works

        Observable<String> slowObservableWithOperations = observable
                .map(this::slowIdentity)//<-- explain how slowIdentity works
                .map(this::slowToString);//<-- explain how slowToString works

        //this is blocking, why?
        slowObservableWithOperations.subscribe(value -> log.info("Value from slow observable {}", value));
    }

    private Observable<Long> createSlowObservable() {
        return Observable.create(emitter -> {
               for(long i = 0; i < 3; ++i){
                   sleep(1000);
                   log.info("Next value {} will be emitted.", i);
                   emitter.onNext(i);
               }
                sleep(1000);
                emitter.onComplete();
                log.info("Slow observable completed");
            });
    }

    private Long slowIdentity(Long value) {
        sleep(1000);
        log.info("Slow identity transformation executed for value {}", value);
        return value;
    }

    private String slowToString(Long integer){
        sleep(1000);
        log.info("Slow to string executed for value {}", integer);
        return integer.toString();
    }

    @SneakyThrows
    private void sleep(long timeout){
        TimeUnit.MILLISECONDS.sleep(timeout);
    }

    @Test
    public void withoutSchedulersTwoSubscribers(){
        Observable<Long> observable = createSlowObservable();

        Observable<String> slowObservableWithOperations = observable
                .map(this::slowIdentity)
                .map(this::slowToString);

        //what is expected test execution time?
        slowObservableWithOperations.subscribe(value -> log.info("Value from subscriber ONE {}", value));
        slowObservableWithOperations.subscribe(value -> log.info("Value from subscriber TWO {}", value));
    }

    @Test
    public void connectableObservable(){
        Observable<Long> observable = createSlowObservable();

        ConnectableObservable<String> connectableObservable = observable
                .map(this::slowIdentity)//<-- how many times slowIdentity will be executed?
                .map(this::slowToString)//<-- how many times slowToString will be executed?
                .publish();// <-- here connectable observable is created

        log.info("Add subscribers");
        connectableObservable.subscribe(value -> log.info("Value from subscriber ONE {}", value));
        connectableObservable.subscribe(value -> log.info("Value from subscriber TWO {}", value));

        log.info("Two subscribers added, now let's connect");
        connectableObservable.connect();//<-- and what is expected test execution time?
    }

    @Test
    public void hotConnectableObservable(){
        Observable<Long> observable = createSlowObservable();

        ConnectableObservable<String> connectableObservable = observable
                .map(this::slowIdentity)//<-- how many times slowIdentity will be executed?
                .map(this::slowToString)//<-- how many times slowToString will be executed?
                .publish();// <-- here connectable observable is created


        log.info("Connect called before two subscriber added.");
        connectableObservable.connect();

        log.info("Two subscriber will be added");
        //How many events subscriber receive
        connectableObservable.subscribe(value -> log.info("Value from subscriber ONE {}", value));
        connectableObservable.subscribe(value -> log.info("Value from subscriber TWO {}", value));
    }

    @Test
    public void shouldSubscribeOnScheduler(){
        Observable<Long> observable = createSlowObservable();

        Observable<String> observableWithTransformations = observable
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribeOn(Schedulers.io());//<-- let's run in in separate thread

        //what does it print?
        observableWithTransformations.subscribe(value -> log.info("Get value {}", value));
    }

    @Test
    public void shouldSubscribeOnScheduler2(){
        Observable<Long> observable = createSlowObservable();

        Observable<String> observableWithTransformations = observable
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribeOn(Schedulers.io());//<-- let's run in in separate thread

        //what does it print?
        observableWithTransformations.subscribe(value -> log.info("Get value {}", value));
        sleep(10000);
    }

    @Test
    public void twoConcurrentSubscriptions(){
        Observable<Long> observable = createSlowObservable();

        Observable<String> observableWithTransformations = observable
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribeOn(Schedulers.io());//<-- let's run in in separate thread

        //how many thread can you see
        observableWithTransformations.subscribe(value -> log.info("Subscriber ONE {}", value));
        observableWithTransformations.subscribe(value -> log.info("Subscriber TWO {}", value));
    }

    @Test
    public void manyInvocationOfSubscribeOnIsPointless(){
        Observable<Long> observable = createSlowObservable();

        Observable<String> observableWithTransformations = observable
                .subscribeOn(Schedulers.computation())//<-- Run it on computation scheduler
                .map(this::slowIdentity)
                .subscribeOn(Schedulers.newThread())//<-- Run it on New Thread scheduler
                .map(this::slowToString)
                .subscribeOn(Schedulers.io());//<-- Run it on IO scheduler

        //which schedulers are used?
        observableWithTransformations.subscribe(value -> log.info("Got value {}", value));
        sleep(10000);
    }

    @Test
    public void shouldObserveOnOtherScheduler(){
        Observable<Long> observable = createSlowObservable();

        Observable<String> observableWithTransformations = observable
                .observeOn(Schedulers.computation())//<-- Observe on computation scheduler
                .map(this::slowIdentity)
                .observeOn(Schedulers.newThread())//<-- Observe on New Thread scheduler
                .map(this::slowToString)
                .subscribeOn(Schedulers.io());

        //which schedulers are used?
        observableWithTransformations.subscribe(value -> log.info("Got value {}", value));
        sleep(10000);
    }

    @Test
    public void shouldExecuteBlockingGet(){
        Observable<Long> observable = createSlowObservable();

        Observable<String> observableWithTransformations = observable
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribeOn(Schedulers.io());//<-- run it on IO scheduler

        //what does it print, in which thread?
        observableWithTransformations.blockingForEach(value -> log.info("Get value {} in blocking manner.", value));
        //there is plenty of blocking... method
    }

    @Test
    public void manyMethodAcceptScheduler(){
        Observable.interval(100, TimeUnit.MILLISECONDS, Schedulers.io()) //<-- use provided scheduler
                .timeout(1, TimeUnit.SECONDS, Schedulers.computation())//<-- use provided scheduler
                .take(3)
                .subscribe(value -> log.info("Got value {}", value));
        sleep(2000);
    }

    @Test
    public void eventsEmittedTooQuickly(){
        Observable.interval(1, TimeUnit.MILLISECONDS, Schedulers.computation())//<-- emits 1000 event per second
                .take(5)
                .map(this::slowIdentity)//<-- takes one second
                .map(this::slowToString)//<-- takes one second
                .subscribe(value -> log.info("How many message per sec will be printed?"));
        sleep(10100);
    }

    @Test
    public void eventsEmittedTooQuickly2(){
        Observable.interval(1, TimeUnit.MILLISECONDS, Schedulers.computation())//<-- emits 1000 event per second
                .take(5)
                .observeOn(Schedulers.io()) //<-- scheduler added
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribe(value -> log.info("How many message per sec will be printed? Is it better?"));
        sleep(10100);
    }

    @Test
    public void eventsEmittedTooQuickly3IdAnyBetterWithFlatMap(){
        Observable.interval(1, TimeUnit.MILLISECONDS, Schedulers.computation())//<-- emits 1000 event per second
                .take(5)
                .observeOn(Schedulers.io()) //<-- scheduler added
                .flatMap(number -> Observable.just(slowIdentity(number)))
                .flatMap(number -> Observable.just(slowToString(number)))
                .subscribe(value -> log.info("How many message per sec will be printed? Is it better?"));
        sleep(10100);
    }

    @Test
    public void eventsEmittedTooQuickly4FlatMapAndSubscribeOn() {
        Observable.interval(1, TimeUnit.MILLISECONDS, Schedulers.computation())//<-- emits 1000 event per second
                .take(5)
                .flatMap(value -> Observable.just(value).map(this::slowIdentity).subscribeOn(Schedulers.io()))//<-- flatMap + subscribeOn
                .flatMap(value -> Observable.just(value).map(this::slowToString).subscribeOn(Schedulers.io()))//<-- flatMap + subscribeOn
                //^^^ this is declarative concurrency
                .subscribe(value -> log.info("How many message per sec will be printed? What about order? {}", value));
        sleep(3000);
    }

    @Test
    public void withoutConcurrency(){
        log.info("Test start");
        Observable.just(1, 2, 3)
                .flatMap(this::findUserBadDesignMethod)//<-- explain what findUserBadDesignMethod does
                .subscribe(user -> log.info("How much time does it takes to get all 3 users? '{}'", user));
        //test execution takes about 9 second
    }

    @Test
    public void withBrokenDeclarativeConcurrency(){
        log.info("Test start");
        Observable.just(1, 2, 3)
            .flatMap(id -> findUserBadDesignMethod(id).subscribeOn(Schedulers.io()))//<-- method is executed in main thread
            //^ on IO scheduler is executed only subscription to observable returned by findUserBadDesignMethod
            .blockingSubscribe(user -> log.info("Method findUserBadDesignMethod is still executed sequentially in main thread. '{}'", user));
    }

    @Test
    public void withCorrectDeclarativeConcurrency(){
        log.info("Test start");
        Observable.just(1, 2, 3)
                .flatMap(id -> Observable.defer(() ->findUserBadDesignMethod(id)).subscribeOn(Schedulers.io()))
                //^ defer invocation solve the problem, findUserBadDesignMethod is executed on io scheduler in 3 parallel threads
                .blockingSubscribe(user -> log.info("Method findUserBadDesignMethod executed on IO scheduler. '{}'", user));
    }

    private Observable<String> findUserBadDesignMethod(long id){
        log.info("Long findUserBadDesignMethod method started. In which thread method is executed?");
        //this method simulated slow database query or rest service invocation
        sleep(3000);//<-- every long operation should be executed in Observable.create in onSubscribe callback.
        //// Here is not, but it a way to convert legacy API into reactive
        return Observable.just(String.format("User with id %d", id));
    }


    @Test
    public void ownBadScheduler(){
        Scheduler scheduler = Schedulers.from(Executors.newCachedThreadPool());//<-- own scheduler

        Observable<Long> slowObservable = createSlowObservable();

        slowObservable
                .subscribeOn(scheduler)
                .subscribe(value -> log.info("Get value {}", value));
        sleep(3100);
    }

    @Test
    public void ownGoodScheduler(){
         /*@formatter:off*/
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat("custom-descriptive-thread-name" + "-%d")//<-- very important, present in logs and JMX
                .setUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception in RxJava scheduler thread {}", t.getName(), e))//<-- all exceptions must be logged.
                .build();
        ExecutorService executorService = new ThreadPoolExecutor(3,//<-- three thread almost always available
                15,//<-- do not create more then 15 threads
                30, TimeUnit.SECONDS,//<-- release thread after 30s if there is not task for it
                new LinkedBlockingQueue<>(15),//<-- if no thread available submit no more then 15 task in non blocking fashion
                new ThreadFactoryLoggableWrapper(threadFactory),//<-- log thread creation
                new ThreadPoolExecutor.CallerRunsPolicy());//<-- sometime it is better to return error
        /*@formatter:on*/
        Scheduler goodScheduler = Schedulers.from(executorService);//here is good scheduler<--

        Observable<Long> slowObservable = createSlowObservable();

        slowObservable
                .subscribeOn(goodScheduler)
                .subscribe(value -> log.info("Get value {}", value));
        sleep(3100);
    }

    @RequiredArgsConstructor
    private static class ThreadFactoryLoggableWrapper implements ThreadFactory{

        private final ThreadFactory threadFactory;

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = threadFactory.newThread(r);
            log.debug("New thread '{}' created for RxJava scheduler", thread.getName());
            return thread;
        }
    }
}
