package com.impaqgroup.training.reactive.ex03advancedcreate;

import org.junit.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Ex03AmqpTest {

    public static final String QUEUE_NAME = "RxJavaQueue";

    private CachingConnectionFactory connectionFactory;

    @Before
    public void setUp(){
        this.connectionFactory = new CachingConnectionFactory(5672);
    }

    @After
    public void turnDown(){
        this.connectionFactory.destroy();
    }

    @Test
    public void shouldGetAmqpMessage(){
        String messageContent = createAmqpObservable(QUEUE_NAME)
                .map(Message::getBody)
                .map(String::new)
                .blockingFirst();//<-- wait for message
        log.info("Amqp message received '{}'", messageContent);
    }

    private Observable<Message> createAmqpObservable(String queueName){
        Observable<Message> observable = Observable.create(emitter -> {
            try {
                log.info("Creating AMQP messages observable.");
                SimpleMessageListenerContainer container = listenerContainerRabbitMq(connectionFactory, queueName, (message) -> {
                    log.info("Message will be push to observable stream {}", message);
                    emitter.onNext(message);//<-- push AMQP messages into observable stream.
                });
                emitter.setCancellable(() -> container.stop());//<-- release resources
                log.debug("AMQP listener created");
            }catch (Throwable t){
                emitter.onError(t);
            }
        });
        return observable;//store somewhere and close
    }

    private SimpleMessageListenerContainer listenerContainerRabbitMq(ConnectionFactory connectionFactory, String queueName, MessageListener messageListener) {
        log.info("Listener container will be created.");
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setAutoDeclare(false);
        container.setMissingQueuesFatal(true);
        container.setMessageListener(messageListener);
        container.setMaxConcurrentConsumers(1);//<-- use only single thread to read messages from queue
        container.start();
        return container;
    }

}
