package com.impaqgroup.training.reactive.other;

import static java.lang.Math.random;

import org.junit.Test;

import io.reactivex.Observable;

public class RandomNumberObservableTest {

    @Test
    public void randomNumberObservable(){
        Observable<Double> o = Observable.create(emitter ->{
            while (!emitter.isDisposed()){
                emitter.onNext(random());
            }
            emitter.onComplete();
        });
    }

}
