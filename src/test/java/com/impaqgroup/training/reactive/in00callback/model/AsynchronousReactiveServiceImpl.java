package com.impaqgroup.training.reactive.in00callback.model;

import java.util.concurrent.TimeUnit;

import com.impaqgroup.training.reactive.in00callback.RxAsynchronousTest;

import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsynchronousReactiveServiceImpl implements RxAsynchronousTest.AsynchronousReactiveService {

    @Override
    public Single<Person> findPerson(String userId) {
        return Single
                .just(new Person(userId, "Albert"))
                .delay(randomDelay(), TimeUnit.MILLISECONDS)
                .doOnSuccess(person -> log.info("We have person"));
    }

    @Override
    public Single<Address> findAddress(Person person) {
        return Single
                .just(new Address(5L, "Warsaw", "Długa"))
                .delay(randomDelay(), TimeUnit.MILLISECONDS)
                .doOnSuccess(address -> log.info("We have address"));
    }

    @Override
    public Single<Coordinates> findCoorinates(Address address) {
        return Single
                .just(new Coordinates(2.0, 3.0))
                .delay(randomDelay(), TimeUnit.MILLISECONDS)
                .doOnSuccess(person -> log.info("We have coordinates"));
    }

    private long randomDelay() {
        return (long) (Math.random() * 3000);
    }
}
