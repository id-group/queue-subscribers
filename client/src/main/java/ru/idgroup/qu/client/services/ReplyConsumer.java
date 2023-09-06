package ru.idgroup.qu.client.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.idgroup.qu.client.dto.SmevResponse;

@Component
@Slf4j
public class ReplyConsumer {


    @RabbitListener(queues = "#{responseQueue.name}", concurrency = "10")
    public void receive(SmevResponse smevResponse, Message message) {
        String correlationId = message.getMessageProperties().getCorrelationId();
        log.info("Получен ответ correlationId {} body {}", correlationId, smevResponse.getSomeResponseData());

        //сохранение в БД
        //registrationService.saveRegistration(UUID.fromString(correlationId), registrationDto);
    }
}
