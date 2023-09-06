package ru.idgroup.qu.client.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String MAIN_EXCHANGE = "rec-core-exchange";
    public static final String REC_CORE_SMEV_RESPONSE_QUEUE = "rec-core-smev-response-queue";

    @Bean
    public MessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(MAIN_EXCHANGE);
    }

    @Bean
    public Queue responseQueue(){
        return new Queue(REC_CORE_SMEV_RESPONSE_QUEUE);
    }

    @Bean
    public Binding responseQueueBinding(){
        return BindingBuilder.bind(responseQueue())
                .to(topicExchange())
                .with("rec.core.smev.response");
    }



}
