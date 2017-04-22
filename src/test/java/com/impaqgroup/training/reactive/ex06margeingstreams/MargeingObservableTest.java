package com.impaqgroup.training.reactive.ex06margeingstreams;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.Observable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MargeingObservableTest {

    private static final int TIME_PER_LETTER = 50;
    public static final String LYRICS_AEROSMITH = "Yeah, I know nobody knows\n" + "Where it comes and where it goes\n" + "I know it's everybody's sin\n"
            + "You got to lose to know how to win";
    public static final String LYRICS_MUSE = "Don't waste your time or time will waste you";
    public static final String LYRICS_METALLICA = "Musha ring dum a doo dum a da\n" + "Whack for my daddy-o";
    public static final String REGEXP_SPLIT_TEXT = "\\s";

    @Test
    @SneakyThrows
    public void shouldSubscribeToObservableWhichEmitsEventFirstEventQuickest(){
        Observable<String> milliseconds = Observable.just("After", "1", "milliseconds").delay(1, TimeUnit.MILLISECONDS);
        Observable<String> seconds = Observable.just("After", "1", "seconds").delay(1, TimeUnit.SECONDS);
        Observable<String> minutes = Observable.just("After", "1", "minutes").delay(1, TimeUnit.MINUTES);

        minutes
                .ambWith(seconds)
                .ambWith(milliseconds)
                .subscribe(text -> log.info("Text: {}", text));

        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    public void shouldMargeValues(){
        Observable<Integer> evenObservable = Observable.just(0, 2, 4, 6, 8);
        Observable<Integer> oddObservable = Observable.just(1, 3, 5, 7, 9);
        Observable<Integer> evenAndOddObservable = Observable.merge(evenObservable, oddObservable);
        evenAndOddObservable.subscribe(number -> log.info("Number {}", number));

    }

    @Test
    public void karaokeObservableTest(){
        Observable<String> karaokeObservable = karaokeObservable(LYRICS_AEROSMITH);
        karaokeObservable.subscribe(log::info);
        karaokeObservable.toList().blockingGet();
    }

    @Test
    public void shouldMargeObservable(){
        Observable<String> aerosmith = karaokeObservable(LYRICS_AEROSMITH).map(word -> String.format("a: %s", word));
        Observable<String> muse = karaokeObservable(LYRICS_MUSE).map(word -> String.format("m: %s", word));
        Observable<String> metallica = karaokeObservable(LYRICS_METALLICA).map(word -> String.format("M: %s", word));

        Observable<String> mergedObservable = Observable.merge(aerosmith, muse, metallica); //<-- all are subscribed simultaneously

        mergedObservable.subscribe(log::info);
        mergedObservable.toList().blockingGet();
    }

    @Test
    public void shouldConcatObservable(){
        Observable<String> aerosmith = karaokeObservable(LYRICS_AEROSMITH).map(word -> String.format("a: %s", word));
        Observable<String> muse = karaokeObservable(LYRICS_MUSE).map(word -> String.format("m: %s", word));
        Observable<String> metallica = karaokeObservable(LYRICS_METALLICA).map(word -> String.format("M: %s", word));

        Observable<String> concatedObservable = Observable.concat(aerosmith, muse, metallica);//one after another is subscribed

        concatedObservable.subscribe(log::info);
        concatedObservable.toList().blockingGet();
    }

    @Test
    public void shouldZipObservable(){
        Observable<Integer> counterObservable = Observable.range(0, 30);
        Observable<String> karaokeObservable = karaokeObservable(LYRICS_AEROSMITH);

        Observable<String> zipObservable = Observable
                .zip(counterObservable, karaokeObservable, (number, word) -> String.format("Word number %d is '%s'", number, word));

        zipObservable.subscribe(log::info);
        zipObservable.toList().blockingGet();
    }

    private Observable<String> karaokeObservable(String lyrics){
        return Observable.fromArray(lyrics.split(REGEXP_SPLIT_TEXT))
            .map(String::length)
            .map(length -> length * TIME_PER_LETTER)
            .scan(Math::addExact)
            .flatMap(wordSummaryDelay -> Observable
                    .just(wordSummaryDelay)
                    .delay(wordSummaryDelay, TimeUnit.MILLISECONDS))
            .startWith(0)
            .skipLast(1)
            .zipWith(splitText(lyrics), (delay, word) -> word);
    }

    private Observable<String> splitText(String text){
        return Observable.fromArray(text.split(REGEXP_SPLIT_TEXT));
    }

}
