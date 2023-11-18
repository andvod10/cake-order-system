package com.serverless.apigateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;
import lombok.extern.log4j.Log4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Log4j
public class ApiGatewayService {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public String buildBodyForResponse(Object body) {
        String rawBody;
        if (body instanceof String) {
            rawBody = (String) body;
        } else if (body instanceof byte[]) {
            rawBody = new String(Base64.getEncoder().encode((byte[]) body), StandardCharsets.UTF_8);
        } else {
            try {
                rawBody = objectMapper.writeValueAsString(body);
            } catch (JsonProcessingException e) {
                log.error("failed to serialize object", e);
                throw new RuntimeException(e);
            }
        }
        return rawBody;
    }

    @Value
    public static class Response {

        String message;
        Map<String, Object> input;

        public Response(String message, Map<String, Object> input) {
            this.message = message;
            this.input = input;
        }
    }
}
