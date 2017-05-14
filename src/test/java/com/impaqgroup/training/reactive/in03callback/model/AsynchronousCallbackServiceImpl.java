package com.impaqgroup.training.reactive.in03callback.model;

import java.util.concurrent.*;
import java.util.function.Consumer;

import com.impaqgroup.training.reactive.in03callback.CallbackAsynchronousTest;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsynchronousCallbackServiceImpl implements CallbackAsynchronousTest.AsynchronousCallbackService {

    private final ExecutorService executorService;

    public AsynchronousCallbackServiceImpl() {
        this.executorService = Executors.newFixedThreadPool(2);//In production dependence injection need here
    }

    @Override
    public void findPerson(String userId, Consumer<Person> onSuccess, Consumer<Throwable> onError) {
        executorService.submit(() -> {
            requestToServerSymulation();
            log.info("Person found");
            onSuccess.accept(new Person(userId, "Budzigniew"));
        });
    }

    @Override
    public void findAddress(Person person, Consumer<Address> onSuccess, Consumer<Throwable> onError) {
        executorService.submit(()->{
            requestToServerSymulation();
            log.info("Address found");
            onSuccess.accept(new Address(1L, "Warsaw", "Krakowskie Przedmieście"));
        });
    }

    @Override
    public void findCoorinates(Address address, Consumer<Coordinates> onSuccess, Consumer<Throwable> onError) {
        executorService.submit(()->{
            requestToServerSymulation();
            log.info("Coordinates found");
            onSuccess.accept(new Coordinates(Math.PI, Math.E));
        });
    }

    @SneakyThrows
    private void requestToServerSymulation(){
        long delay = (long) (Math.random() * 3000);
        TimeUnit.MILLISECONDS.sleep(delay);
    }
}
