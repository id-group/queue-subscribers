package ru.idgroup.qu.server.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import ru.idgroup.qu.server.dto.*;

import java.time.Duration;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RestConsumerService {

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange topicExchange;
    private final Map<String, SubsribeData> subscribes = new HashMap<>();

    public SmevRequest post(String corrId, String replyTo, SmevRequest smevRequest) {
        var request = SmevQueueRequest.builder()
                .correlationId(corrId)
                .replyTo(replyTo)
                .payload(smevRequest)
                .build();

        rabbitTemplate.convertAndSend(topicExchange.getName(),
                "smev.inner.rest.request",
                request);

        return smevRequest;
    }

    public void subscribeService(SubsribeData subsribeData) {
        subscribes.put(subsribeData.getReplyKey(), subsribeData);
    }


    @RabbitListener(queues = "#{innerRestRequestQueue.name}", concurrency = "10")
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
                "smev.inner.rest.response",
                smevQueueResponse);
        log.info("Сформирован ответ correlationId {} во внутренней очереди.", smevQueueRequest.getCorrelationId());
    }

    @RabbitListener(queues = "#{innerRestResponseQueue.name}", concurrency = "10")
    public void responseInner(SmevQueueResponse smevQueueResponse) {
        log.info("Получен ответ correlationId {} из внутренней очереди.",smevQueueResponse.getCorrelationId());

        var subscriber = subscribes.get(smevQueueResponse.getReplyTo());
        if(Objects.nonNull(subscriber)) {

            //todo сохранять в кэше
            var webClient = WebClient.builder()
                    .baseUrl(subscriber.getReplyUrl())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .clientConnector(
                            new ReactorClientHttpConnector(HttpClient
                                    .create()
                                    .responseTimeout(Duration.ofSeconds(5)))
                    )
                    .build();

            webClient.post()
                    .header("correlation-id")
                    .body(BodyInserters.fromValue(smevQueueResponse.getPayload()))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Сформирован ответ correlationId {} в очередь {}.", smevQueueResponse.getReplyTo());
        }
    }

}
