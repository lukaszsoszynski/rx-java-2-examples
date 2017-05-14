package com.impaqgroup.training.reactive.in01Thread;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import org.junit.Test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadTest {

    @Test
    public void shouldRunManyThreads(){
        while (true){
            //log.info("New thread started");
            new Thread(() -> {
                sleep(30, SECONDS);
                log.info("How many times this text will be printed?");
            }).start();
        }
    }

    @Test
    public void shouldUseThreadPool(){
        //run with -Xmx64M to see what happen (task queue, infinite size)
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        while (true){
            executorService.submit(()->{
                sleep(30, SECONDS);
                log.info("How many times this text will be printed?");
            });
        }
    }

    @Test
    public void shouldUseThreadPoolBetter(){
        LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>(15);//<-- most important line
        ExecutorService executorService = new ThreadPoolExecutor(3, 3, 0L, MILLISECONDS, taskQueue);
        while (true){
            executorService.submit(()->{
                sleep(30, SECONDS);
                log.info("How many times this text will be printed?");
            });
        }
    }

    @Test
    public void shouldUseThreadPoolEvenBetter(){
        LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>(15);//<-- most important line
        RejectedExecutionHandler rejectedExecutionHandler = new CallerRunsPolicy();
        ExecutorService executorService = new ThreadPoolExecutor(3, 3, 0L, MILLISECONDS, taskQueue, Executors.defaultThreadFactory(), rejectedExecutionHandler);

        while (true){
            executorService.submit(()->{
                sleep(30, SECONDS);
                log.info("How many times this text will be printed?");
            });
            log.info("New task added to pool");
        }
    }


    @SneakyThrows
    private void sleep(int timeout, TimeUnit timeUnit){
        timeUnit.sleep(timeout);
    }

}
