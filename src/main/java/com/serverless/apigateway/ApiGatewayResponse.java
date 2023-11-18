package com.serverless.apigateway;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Builder
@Data
public class ApiGatewayResponse{
    @Builder.Default
    private int statusCode = 200;
    @Builder.Default
    private Map<String, String> headers = Collections.emptyMap();
    private String body;
    private boolean isBase64Encoded;

}
