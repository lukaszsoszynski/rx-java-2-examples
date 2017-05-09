package com.impaqgroup.training.reactive.in00callback;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import com.impaqgroup.training.reactive.in00callback.model.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CallbackAsynchronousTest {

    public static final String USER_ID = "user id";
    private static final Address ADDRESS = new Address(23L, "New York", "Long");

    public interface AsynchronousCallbackService{

        void findPerson(String userId, Consumer<Person> onSuccess, Consumer<Throwable> onError);

        void findAddress(Person person, Consumer<Address> onSuccess, Consumer<Throwable> onError);

        void findCoorinates(Address address, Consumer<Coordinates> onSuccess, Consumer<Throwable> onError);

    }

    private AsynchronousCallbackServiceImpl service;

    @Before
    public void setUp(){
        this.service = new AsynchronousCallbackServiceImpl();
    }

    @Test
    public void shouldFindUserCoordinatesByUserId() throws InterruptedException {
        Holder<Coordinates> holder = new Holder<>();
        CountDownLatch compleationSignal = new CountDownLatch(1);
        service.findPerson(USER_ID, person -> {
            service.findAddress(person, address -> {
                service.findCoorinates(address, coordinates -> {
                    log.info("We have got coordinates {}", coordinates);
                    holder.setValue(coordinates);
                    compleationSignal.countDown();
                }, ex -> log.error("Error handling here", ex));
            }, ex -> log.error("Error handling here", ex));
        }, ex -> log.error("Error handling here", ex));
        compleationSignal.await();//<-- waits forever in case of error
        log.info("User '{}' Coordinates {}", USER_ID, holder.getValue());
    }

    @Test
    public void shouldGetUserAndCoordinatesInParallel() throws InterruptedException {
        CountDownLatch compleationSignal = new CountDownLatch(2);
        Holder<Person> personHolder = new Holder<>();
        Holder<Coordinates> coordinatesHolder = new Holder<>();

        //two operations happens in parallel (findPerson, findCoorinates)
        service.findPerson(USER_ID, person -> {
            personHolder.setValue(person);
            compleationSignal.countDown();
        }, ex -> log.error("Error handling here", ex));
        service.findCoorinates(ADDRESS, coordinates -> {
            coordinatesHolder.setValue(coordinates);
            compleationSignal.countDown();
        }, ex -> log.error("Error handling here", ex));

        //both operation may accomplish in different time. It is our responsibility
        //to wait for completion of both
        compleationSignal.await();//<-- waits forever in case of error
        PersonPosition personPosition = new PersonPosition(personHolder.getValue(), coordinatesHolder.getValue());
        log.info("Person position: {}", personPosition);
    }

}



