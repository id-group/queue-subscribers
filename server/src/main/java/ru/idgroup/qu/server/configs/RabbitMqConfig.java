package ru.idgroup.qu.server.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String MAIN_EXCHANGE = "rec-core-exchange";
    public static final String REC_CORE_SMEV_REQUEST_QUEUE = "rec-core-smev-request-queue";
    public static final String SMEV_INNER_REQUEST_QUEUE = "smev-inner-request-queue";
    public static final String SMEV_INNER_RESPONSE_QUEUE = "smev-inner-response-queue";

    @Bean
    public MessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(MAIN_EXCHANGE);
    }

    @Bean
    public Queue requestQueue(){
        return new Queue(REC_CORE_SMEV_REQUEST_QUEUE);
    }

    @Bean
    public Binding recCoreSmevRequestBinding(){
        return BindingBuilder.bind(requestQueue())
                .to(topicExchange())
                .with("rec.core.smev.request");
    }

    @Bean
    public Queue innerRequestQueue(){
        return new Queue(SMEV_INNER_REQUEST_QUEUE);
    }

    @Bean
    public Binding innerRequestBinding(){
        return BindingBuilder.bind(innerRequestQueue())
                .to(topicExchange())
                .with("smev.inner.request");
    }

    @Bean
    public Queue innerResponseQueue(){
        return new Queue(SMEV_INNER_RESPONSE_QUEUE);
    }

    @Bean
    public Binding innerResponseBinding(){
        return BindingBuilder.bind(innerResponseQueue())
                .to(topicExchange())
                .with("smev.inner.response");
    }



}
