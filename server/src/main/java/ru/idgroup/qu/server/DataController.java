package ru.idgroup.qu.server;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import ru.idgroup.qu.server.dto.SmevQueueRequest;
import ru.idgroup.qu.server.dto.SmevRequest;
import ru.idgroup.qu.server.dto.SubsribeData;
import ru.idgroup.qu.server.services.RestConsumerService;

import java.util.Map;

@RestController
@RequestMapping("/v1/service")
@RequiredArgsConstructor
public class DataController {
    private final RestConsumerService consumerService;

    @PostMapping("/data")
    public SmevRequest putData(@RequestBody SmevRequest smevRequest, @RequestHeader Map<String, String> headers) {
        String corrId = headers.get("correlation-id");
        String replyTo = headers.get("reply-to");

        return consumerService.post(corrId, replyTo, smevRequest);
    }

    @PostMapping("/subscribe")
    public void subscribe(@RequestBody SubsribeData subsribeData) {
        consumerService.subscribeService(subsribeData);
    }
}
