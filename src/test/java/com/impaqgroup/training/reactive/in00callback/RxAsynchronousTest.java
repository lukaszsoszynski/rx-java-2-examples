package com.impaqgroup.training.reactive.in00callback;

import static io.reactivex.Single.zip;
import static io.reactivex.schedulers.Schedulers.io;

import org.junit.Before;
import org.junit.Test;

import com.impaqgroup.training.reactive.in00callback.model.*;

import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RxAsynchronousTest {

    public static final String USER_ID = "user id";
    private static final Address ADDRESS = new Address(54L, "City", "Street");

    public interface AsynchronousReactiveService{

        Single<Person> findPerson(String userId);

        Single<Address> findAddress(Person person);

        Single<Coordinates> findCoorinates(Address address);

    }

    private AsynchronousReactiveService service;

    @Before
    public void setUp(){
        this.service = new AsynchronousReactiveServiceImpl();
    }

    @Test
    public void shouldFindUserCoordinatesByUserId(){
        Coordinates coordinates = service
                .findPerson(USER_ID)
                .flatMap(service::findAddress)
                .flatMap(service::findCoorinates)
                .subscribeOn(io())
                .blockingGet();
        log.info("User '{} coordinates {}",USER_ID,  coordinates);
    }

    @Test
    public void shouldGetUserAndCoordinatesInParallel(){
        PersonPosition personPosition = zip(service.findPerson(USER_ID).observeOn(io()),
                service.findCoorinates(ADDRESS).observeOn(io()),
                PersonPosition::new)
                .blockingGet();
        log.info("Person position: {}", personPosition);
    }
}
