package ru.idgroup.qu.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmevRequest {
    private String requestId;
    private String someRequestData;
}
