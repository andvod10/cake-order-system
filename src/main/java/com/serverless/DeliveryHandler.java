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
import com.serverless.order.Order;
import com.serverless.order.OrderManager;
import lombok.extern.log4j.Log4j;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Log4j
public class DeliveryHandler implements RequestHandler<APIGatewayProxyRequestEvent, ApiGatewayResponse> {

    private final static ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final static OrderManager orderManager = new OrderManager();
    private final static ApiGatewayService apiGatewayService = new ApiGatewayService();

    @Override
    public ApiGatewayResponse handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        OrderRequest orderRequest;
        try {
            orderRequest = objectMapper.readValue(event.getBody(), OrderRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (Objects.isNull(orderRequest.getOrderReview())
                || orderRequest.getOrderReview() < 1
                || orderRequest.getOrderReview() > 5
        ) {
            Map<String, Object> errors = Map.of("validation error", "OrderReview should be in range 1 to 5");
            ApiGatewayService.Response responseBody = new ApiGatewayService.Response("Success!", errors);
            return ApiGatewayResponse.builder()
                    .statusCode(400)
                    .body(apiGatewayService.buildBodyForResponse(responseBody))
                    .headers(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                    .isBase64Encoded(true)
                    .build();
        }

        log.info("Order to delivery: " + orderRequest);
        Order order = orderManager.deliveryOrder(orderRequest);

        Map<String, Object> orderSummary = objectMapper.convertValue(
                order,
                new TypeReference<>() {
                }
        );

        ApiGatewayService.Response responseBody = new ApiGatewayService.Response("Success!", orderSummary);
        return ApiGatewayResponse.builder()
                .statusCode(200)
                .body(apiGatewayService.buildBodyForResponse(responseBody))
                .headers(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                .isBase64Encoded(true)
                .build();
    }

}
