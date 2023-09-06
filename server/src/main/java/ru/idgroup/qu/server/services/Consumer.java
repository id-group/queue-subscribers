package ru.idgroup.qu.server.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.idgroup.qu.server.dto.SmevQueueRequest;
import ru.idgroup.qu.server.dto.SmevQueueResponse;
import ru.idgroup.qu.server.dto.SmevRequest;
import ru.idgroup.qu.server.dto.SmevResponse;

import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
public class Consumer {
    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange topicExchange;

    @RabbitListener(queues = "#{requestQueue.name}", concurrency = "10")
    public void receive(SmevRequest smevRequest,
                                @Header(AmqpHeaders.CORRELATION_ID) String correlationId,
                                @Header(AmqpHeaders.REPLY_TO) String replyTo
    ) {
        log.info("Получен запрос correlationId {} replyTo {}", correlationId, replyTo);
        var smevQueueRequest = SmevQueueRequest.builder()
                .correlationId(correlationId)
                .replyTo(replyTo)
                .payload(smevRequest)
                .build();

        rabbitTemplate.convertAndSend(topicExchange.getName(),
                "smev.inner.request",
                smevQueueRequest);

        log.info("Запрос correlationId {} поставлено во внутреннюю очередь.", correlationId);
    }

    @RabbitListener(queues = "#{innerRequestQueue.name}", concurrency = "10")
    public void receiveInner(SmevQueueRequest smevQueueRequest) {
        log.info("Запрос correlationId {} получен из внутренней очереди.", smevQueueRequest.getCorrelationId());



        var smevQueueResponse = SmevQueueResponse.builder()
                        .correlationId(smevQueueRequest.getCorrelationId())
                        .replyTo(smevQueueRequest.getReplyTo())
                        .payload(SmevResponse.builder()
                                .requestId(smevQueueRequest.getPayload().getRequestId())
                                .someResponseData("ответ")
                                .build()
                        )
                        .build();

        rabbitTemplate.convertAndSend(topicExchange.getName(),
                "smev.inner.response",
                smevQueueResponse);
        log.info("Сформирован ответ correlationId {} во внутренней очереди.", smevQueueRequest.getCorrelationId());
    }

    @RabbitListener(queues = "#{innerResponseQueue.name}", concurrency = "10")
    public void responseInner(SmevQueueResponse smevQueueResponse) {
        log.info("Получен ответ correlationId {} из внутренней очереди.",smevQueueResponse.getCorrelationId());

        MessagePostProcessor messagePostProcessor = message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            messageProperties.setCorrelationId(smevQueueResponse.getCorrelationId());
            return message;
        };

        rabbitTemplate.convertAndSend(topicExchange.getName(),
                smevQueueResponse.getReplyTo(),
                smevQueueResponse.getPayload(),
                messagePostProcessor);
        log.info("Сформирован ответ correlationId {} в очередь {}.", smevQueueResponse.getReplyTo());
    }


}
