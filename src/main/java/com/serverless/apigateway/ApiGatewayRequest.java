package com.serverless.apigateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
//@NoArgsConstructor
//@RequiredArgsConstructor
//@AllArgsConstructor
public class ApiGatewayRequest implements Serializable {

    @Builder.Default
    private final Map<String, String> headers = Collections.emptyMap();
    @JsonProperty("body")
    private final OrderRequest body;

}
