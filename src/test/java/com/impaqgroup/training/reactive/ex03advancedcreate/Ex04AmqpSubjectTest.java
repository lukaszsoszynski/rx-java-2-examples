package com.impaqgroup.training.reactive.ex03advancedcreate;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class Ex04AmqpSubjectTest {

    private AmqpObserverService amqpObserverService;

    @Mock
    private Message message;

    @Before
    public void setUp(){
        when(message.getBody()).thenReturn("message body".getBytes());
        this.amqpObserverService = new AmqpObserverService();
    }

    @Test
    public void shouldUsePublicSubject(){
        //given
        amqpObserverService.amqpObservable()
                .map(Message::getBody)
                .map(String::new)
                .subscribe(messageBody -> log.info("New message received '{}'", messageBody));

        //when amqp message arrives
        amqpObserverService.onMessage(message);
    }

    public static class AmqpObserverService {

        private final PublishSubject<Message> publishSubject;//<-- can be used to introduce reactive code into existing projects

        public AmqpObserverService(){
            this.publishSubject = PublishSubject.create();//<-- create subject
        }

        public Observable<Message> amqpObservable(){
            return publishSubject;
        }

        @RabbitListener(queues = "myAmqpQueue")
        public void onMessage(Message message){
            publishSubject.onNext(message);//<--- push some events through it
        }
    }

}
