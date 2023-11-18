package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serverless.apigateway.ApiGatewayResponse;
import com.serverless.apigateway.ApiGatewayService;
import com.serverless.apigateway.OrderRequest;
import com.serverless.order.Order;
import com.serverless.order.OrderManager;
import lombok.extern.log4j.Log4j;

import java.util.Collections;
import java.util.Map;

@Log4j
public class FulfillHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private final static ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final static OrderManager orderManager = new OrderManager();

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> event, Context context) {
        OrderRequest orderRequest;
        try {
            orderRequest = objectMapper.readValue((String) event.get("body"), OrderRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("Order to fulfillment: " + orderRequest);
        Order order = orderManager.fulfillOrder(orderRequest);

        Map<String, Object> orderSummary = objectMapper.convertValue(
                order,
                new TypeReference<>() {
                }
        );

        ApiGatewayService.Response responseBody = new ApiGatewayService.Response("Success!", orderSummary);
        ApiGatewayService apiGatewayService = new ApiGatewayService();
        return ApiGatewayResponse.builder()
                .statusCode(200)
                .body(apiGatewayService.buildBodyForResponse(responseBody))
                .headers(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                .isBase64Encoded(true)
                .build();
    }
}
