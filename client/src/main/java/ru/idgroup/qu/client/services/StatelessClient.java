package ru.idgroup.qu.client.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.idgroup.qu.client.dto.SmevRequest;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatelessClient {
    private final Queue responseQueue;
    private final RabbitTemplate template;
    private final TopicExchange topicExchange;

    @Scheduled(fixedDelay = 5000)
    public void sendAndForget() {

        SmevRequest smevRequest = SmevRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .someRequestData("Запрос")
                .build();

        UUID correlationId = UUID.randomUUID();
        //smevService.saveSmev(carDto, correlationId);

        MessagePostProcessor messagePostProcessor = message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            messageProperties.setReplyTo("rec.core.smev.response");
            messageProperties.setCorrelationId(correlationId.toString());
            return message;
        };

        template.convertAndSend(topicExchange.getName(),
                "rec.core.smev.request",
                smevRequest,
                messagePostProcessor);
        log.info("Запрос отправлен correlationId {}", correlationId);
    }
}
