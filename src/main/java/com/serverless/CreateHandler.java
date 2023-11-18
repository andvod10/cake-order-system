package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serverless.apigateway.ApiGatewayResponse;
import com.serverless.apigateway.ApiGatewayService;
import com.serverless.apigateway.OrderRequest;
import com.serverless.mapper.OrderMapper;
import com.serverless.order.Order;
import com.serverless.order.OrderManager;
import lombok.extern.log4j.Log4j;

import java.util.Collections;
import java.util.Map;

@Log4j
public class CreateHandler implements RequestHandler<APIGatewayProxyRequestEvent, ApiGatewayResponse> {

    private final static ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final static OrderManager orderManager = new OrderManager();

    @Override
    public ApiGatewayResponse handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        OrderRequest orderRequest;
        try {
            orderRequest = objectMapper.readValue(event.getBody(), OrderRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Order order = OrderMapper.INSTANCE.toOrderPlaced(orderRequest);

        log.info("Create order: " + order);
        orderManager.placeNewOrder(order);

        Map<String, Object> orderSummary = objectMapper.convertValue(
                order,
                new TypeReference<>() {
                }
        );

        ApiGatewayService.Response responseBody = new ApiGatewayService.Response("Success!", orderSummary);
        ApiGatewayService apiGatewayService = new ApiGatewayService();
        return ApiGatewayResponse.builder()
                .statusCode(201)
                .body(apiGatewayService.buildBodyForResponse(responseBody))
                .headers(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                .isBase64Encoded(true)
                .build();
    }
}
