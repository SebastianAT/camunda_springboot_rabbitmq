package com.example.workflow.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Configuration
public class CamundaMessageExchange {

    public static final String topicExchangeName = "camunda-exchange";
    static final String queueName = "messages";
    Map<String, Object> arguments = new HashMap<>();
    

    @Bean
    Queue queue() {
        return new Queue(queueName, false);
    	//arguments.put("x-message-ttl", 5000);
       // return new Queue(queueName, false, false, false, arguments);
        
        /*Queue(String name, boolean durable, boolean exclusive, boolean autoDelete,
    			@Nullable Map<String, Object> arguments)
        arguments={
        		  'x-message-ttl' : 5000, # Delay until the message is transferred in milliseconds.
        		  'x-dead-letter-exchange' : 'amq.direct', # Exchange used to transfer the message from A to B.
        		  'x-dead-letter-routing-key' : 'hello' # Name of the queue we want the message transferred to.
        		}*/
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(topicExchangeName);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("camunda.bpmn.message");
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }
}
