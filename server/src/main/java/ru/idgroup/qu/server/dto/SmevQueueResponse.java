package ru.idgroup.qu.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SmevQueueResponse {
    private SmevResponse payload;
    private String correlationId;
    private String replyTo;
}
