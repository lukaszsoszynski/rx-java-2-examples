package com.impaqgroup.training.reactive.other;

import io.reactivex.disposables.Disposable;

public interface IObserver<T> {

        void onSubscribe(Disposable d);

        void onNext(T t);

        void onError(Throwable e);

        void onComplete();
}
