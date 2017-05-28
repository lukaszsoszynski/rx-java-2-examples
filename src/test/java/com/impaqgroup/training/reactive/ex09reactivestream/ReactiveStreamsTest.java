package com.impaqgroup.training.reactive.ex09reactivestream;

import org.junit.Test;

import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class ReactiveStreamsTest {

    @Test
    public void shouldUseReactiveStream(){
        Flux.just("This", "is", "project", "reactor", "class")
                .map(String::toLowerCase)
                .subscribe(log::info);
    }

    @Test
    public void rxjavaAndProjectReactor(){
        // class from project reactor
        Flux<String> flux = Flux.just("This", "is", "project", "reactor", "class");

        //class form RxJava2
        Flowable<String> flowable = Flowable.just("This", "is", "RxJava", "2", "class");

        //RxJava Flowable zipped with Reactor Flux
        flowable.zipWith(flux, (rxjava, reactor) -> String.format("RxJava: %s,\t\t\tReactor: %s", rxjava, reactor))
        .subscribe(log::info);
    }

}
