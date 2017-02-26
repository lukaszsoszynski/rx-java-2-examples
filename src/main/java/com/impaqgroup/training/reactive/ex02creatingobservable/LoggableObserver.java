package com.impaqgroup.training.reactive.ex02creatingobservable;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggableObserver<T> implements Observer<T> {

    @Override
    public void onSubscribe(Disposable subscription) {
        log.info("I got subscription.");
    }

    @Override
    public void onError(Throwable ex) {
        log.error("Oops, sth went wrong!", ex);
    }

    @Override
    public void onComplete() {
        log.info("Subscription completed");
    }

    @Override
    public void onNext(Object event) {
        log.info("I got next event '{}'", event);
    }
}
