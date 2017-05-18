package com.impaqgroup.training.reactive.ex05operators;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OperatorTest {

    private static final String LYRICS = "Yeah, I know nobody knows\n" + "Where it comes and where it goes\n" + "I know it's everybody's sin\n"
            + "You got to lose to know how to win";
    private static final int TIME_PER_LETTER = 50;

    private int counter = 0;

    @Before
    public void setUp(){
        this.counter = 0;
    }

    @Test
    public void shouldFilterNumbers(){
        Observable
                .fromIterable(NaturalNumbers::new)//<-- show NaturalNumbers iterator definition
                .filter(number -> (number % 2) == 0)
                .take(10)//<-- move it up and see how many numbers will be printed (10?)
                .subscribe(number -> log.info("Even number: {}", number));
    }

    @Test
    public void shouldGetFirstValue(){
        Observable.fromArray(LYRICS.split(" "))
                .first("Or default value")//<-- what type returns method first
                .subscribe(word -> log.info("First word '{}'", word));
    }

    @Test
    public void shouldGetLastValue(){
        Observable
                .fromArray(LYRICS.split(" "))
                .lastElement()
                .subscribe(word -> log.info("Last word '{}'", word));
    }

    @Test
    public void shouldGetElementAt(){
        Observable.fromIterable(NaturalNumbers::new)
                .elementAt(10)
                .subscribe(number -> log.info("This time element at 10 position is present {}", number));
    }

    @Test
    public void shouldMapNumbers(){
        Observable
                .fromIterable(NaturalNumbers::new)
                .map(number -> number - 1)
                .map(number -> number / 2.0)
                .map(number -> String.format("%3.2f", number))
                .skip(5)
                .take(5)
                .subscribe(log::info);
    }

    @Test
    public void shouldSkipAndTakeWhile(){
        Observable
                .fromIterable(NaturalNumbers::new)
                .skipWhile(number -> number < 5)
                .takeWhile(number -> number <= 10)
                .map(String::valueOf)
                .subscribe(log::info);
    }

    @Test
    public void shouldGetDistinctLetters(){
        Observable.fromArray(LYRICS.split(""))
                .distinct(character -> character.toLowerCase())// <-- dangerous for infinite observables
                .map(String::valueOf)
                .subscribe(log::info);
    }

    @Test
    @SneakyThrows
    public void shouldFindNewFilesInPath(){
        Observable.interval(1, TimeUnit.SECONDS)//<-- emits one event per sec
                .flatMapIterable(number -> Arrays.asList(new File("/home/lukasz/tmp").list()))//java 1.0 way...NPE possible here if dir does not exists
                .distinct()//<-- this get only distinct values (huge memory consumption)
                .subscribe(fileName -> log.info("New file found '{}'", fileName));
//        TimeUnit.HOURS.sleep(1); //<- uncomment to test it
    }

    @Test
    public void shouldEmitValueAfterChange(){
        Observable.just(1, 1, 1, 1, 2, 2, 1, 1)
                .distinctUntilChanged()//<-- requires less memory
                .map(String::valueOf)
                .subscribe(log::info);
    }

    @Test
    public void shouldCacheNumbers(){
        Observable<Integer> naturalNumbers = Observable.fromIterable(() -> new NextValueLogWrapper(new NaturalNumbers()))
                .take(3)
                .cache();//<-- very dangerous for long or infinite observables

        naturalNumbers.subscribe(number -> log.info("Got number {}", number));
        naturalNumbers.subscribe(number -> log.info("Got number {}", number));
        naturalNumbers.subscribe(number -> log.info("Got number {}", number));
    }

    @Test
    public void shouldBatchProcessEvent(){
        Observable
                .fromIterable(NaturalNumbers::new)
                .buffer(10)// <-- use some memory
                .take(3)
                .subscribe((List<Integer> list) -> log.info("This is whole list of numbers {}", list));
    }

    @Test
    public void shouldCountEvents(){
        Observable
                .fromArray(LYRICS.split(" "))
                .count()
                .subscribe(count -> log.info("Number of word {}", count));
    }

    @Test
    public void shouldCheckIfContainsValue(){
        Observable
                .fromIterable(NaturalNumbers::new)
                .contains(1492)//<-- this operation is performed on infinite observable!
                .subscribe(b -> log.info("Contains number {}. BTW. it emits false only after observable termination event either with success or error.", b));

    }

    @Test
    public void shouldRepeatEvent(){
        Observable
                .fromIterable(NaturalNumbers::new)
                .take(3)
                .repeat(5)
                .map(String::valueOf)
                .subscribe(log::info);
    }

    @Test
    public void shouldCountFromTenToZero(){
        Observable
                .interval(1, TimeUnit.SECONDS)//<-- like in JavaScript
                .map(number -> 10 - number)
                .doOnNext(number -> log.info("Please pay attention to thread name."))//<-- can be used for diagnostic purpose
                .take(11)
                .blockingForEach(number -> log.info("counting down {}", number));
    }

    @Test
    public void shouldScheduleTaskInTheFuture(){
        log.info("test started");
        Observable
                .timer(500, TimeUnit.MILLISECONDS) //<-- this emits only one event
                .map(number -> "This string is printed a little bit later")
                .blockingForEach(log::info);
    }

    @Test
    public void shouldDelayEvents(){
        Observable
                .fromIterable(NaturalNumbers::new)
                .take(10)
                .doOnNext(number -> log.info("Got number {}", number))
                .delay(1500, TimeUnit.MILLISECONDS)
                .blockingForEach(number -> log.info("All numbers are printed later but almost at the same time {}", number));
    }

    @Test
    public void shouldUseFallbackWhenErrorOccurs(){
        Observable
                .error(new RuntimeException("Something went wrong!"))
                .onErrorResumeNext(Observable.just("Subscribe to this observable when error occurs"))//<-- some kind of fallback
                .subscribe(event -> log.info("Next event {}", event), ex -> log.warn("Error occurred", ex), () -> log.info("Observable completed"));
    }

    @Test
    public void shouldSampleObservable(){
        Observable
                .interval(1, TimeUnit.MILLISECONDS)//<-- very high rate stream
                .sample(1, TimeUnit.SECONDS)//not interested with more events then one per secund
                .take(3)
                .blockingForEach(sample -> log.info("We get one sample per second {}", sample));
    }

    @Test
    public void shouldRetryOperation(){
        Observable.create((ObservableEmitter<Double> emitter) ->{
            try {
                emitter.onNext((double)(1 / counter++));//<--error occurs when counter = 0
                log.info("Value emitted successfully");
            }catch (Exception e){
                log.warn("Error occurred");
                emitter.onError(e);
            }

        })
                .map(String::valueOf)
                .retry(1)//<-- retry operation one time
                .subscribe(log::info);
    }

    @Test
    public void shouldDisplaySingleLetter(){
        Observable.just("Abby", "has", "got", "a", "cat")
            .map(string -> string + " ")
            .flatMap(string -> letterSeparator(string))//<-- flatMap powerful operator
            .subscribe(log::info);
    }

    @Test
    public void shouldScanObservable(){
        Observable.fromIterable(NaturalNumbers::new)
                .take(10)
                .scan(Math::addExact)//<-- emits intermediate values into stream
                .subscribe(number -> log.info("Number emitted from scan {}", number));
    }

    @Test
    public void shouldScanStringObservable(){
        Observable
            .just("Abby", " ", "has", " ", "got", " ", "a", " ", "cat")
            .scan(String::concat)
            .subscribe(word -> log.info("Number emitted from scan '{}'", word));
    }

    @Test
    public void shouldComputeDelayForKaraoke(){
        //also emits event with proper delay
        Observable
                .just(LYRICS)
                .flatMap(lyrics -> Observable.fromArray(lyrics.split(" ")))
                .map(word -> word.length())
                .map(wordLength -> wordLength * TIME_PER_LETTER)
                .skipLast(1)
                .startWith(0) //<-- it is possible to start with array or observable
                .scan(Math::addExact)
                .flatMap(wordDelayFromSongStart -> Observable.just(wordDelayFromSongStart).delay(wordDelayFromSongStart, TimeUnit.MILLISECONDS))
                .blockingForEach(delaySum -> log.info("Word should be displayed after {}", delaySum));
    }

    @Test
    public void shouldReduceStringObservable(){
        String result = Observable
            .just("Abby", " ", "has", " ", "got", " ", "a", " ", "cat")
            .reduce(String::concat)
            .blockingGet();
        log.info("Reducing result: {}", result);
    }

    @Test
    public void shouldReduceNaturalNumbers(){
        Integer sum = Observable
                .fromIterable(NaturalNumbers::new)
                .takeWhile(number -> number <= 100)
                .reduce(Math::addExact)
                .blockingGet();
        log.info("Sum of numbers from 1 to 100 is equal to {}", sum);
    }

    @Test
    public void shouldReduceWithSeed(){
        Integer sum = Observable
                .fromIterable(NaturalNumbers::new)
                .takeWhile(number -> number <= 100)
                .reduce(0, Math::addExact)//<-- not very useful initial value
                .blockingGet();
        log.info("Sum of numbers from 1 to 100 is equal to {}", sum);
    }

    @Test
    public void shouldReduceWithUsefulSeed(){
        javaslang.collection.List resultList = Observable
                .fromIterable(NaturalNumbers::new)
                .takeWhile(number -> number <= 100)
                .reduce(javaslang.collection.List.empty(), (list, number) -> list.append(number))//<-- pass empty list as seed
                .blockingGet();
        log.info("Elements form observable added to list {}", resultList);
    }

    @Test
    public void shouldCollectValues(){
        //same as reduce but accumulator/seed is mutable here
        List<Integer> result = Observable
                .fromIterable(NaturalNumbers::new)
                .takeWhile(number -> number <= 100)
                .collectInto(new ArrayList<Integer>(), (list, number) -> list.add(number))//<-- pass empty list as seed
                .blockingGet();
        log.info("Elements form observable added to list {}", result);
    }

    @Test
    public void shouldUseWindow(){
        Observable
                .fromIterable(NaturalNumbers::new)
                .window(10)//<-- use micro batch, returns Observable<Observable<T>>
                .take(3)
                .flatMap(window -> window.reduce((x, y) -> x + y).toObservable())//<-- sum element in each window
                .subscribe(event -> log.info("Got number {}", event));
    }

    private Observable<String> letterSeparator(String thisStringWillBeSeparated){
        return Observable
                .fromArray(thisStringWillBeSeparated.split(""))
                .map(String::valueOf);
    }

    private static class NaturalNumbers implements Iterator<Integer>{

        private int current = 0;

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Integer next() {
            return ++current;
        }
    }

    /**
     * Prints message when next method in invoked
     * @param <T>
     */
    @RequiredArgsConstructor
    @Slf4j
    private static class NextValueLogWrapper<T> implements Iterator<T>{

        private final Iterator<T> iterator;

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            T value = iterator.next();
            log.debug("Next value {} read from iterator", value);
            return value;
        }

    }
}
