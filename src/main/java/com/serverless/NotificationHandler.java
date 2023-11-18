package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.models.kinesis.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serverless.apigateway.ApiGatewayResponse;
import com.serverless.cakeProducer.CakeProducerManager;
import com.serverless.deliveryCompany.DeliveryManager;
import com.serverless.order.EventType;
import com.serverless.order.Order;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

@Log4j
public class NotificationHandler implements RequestHandler<KinesisEvent, ApiGatewayResponse> {

    private final static ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public ApiGatewayResponse handleRequest(KinesisEvent event, Context context) {
        List<Order> orders = event.getRecords()
                .stream()
                .map(KinesisEvent.KinesisEventRecord::getKinesis)
                .map(Record::getData)
                .map(this::parseRecord)
                .collect(Collectors.toList());

        notifyCakeProducer(orders);
        notifyDeliveryCompany(orders);

        return null;
    }

    private Order parseRecord(ByteBuffer byteBuffer) {
        try {
            byte[] byteArray = new byte[byteBuffer.remaining()];
            byteBuffer.get(byteArray);
            return objectMapper.readValue(byteArray, Order.class);
        } catch (IOException e) {
            log.error("Error when parsing events: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void notifyCakeProducer(List<Order> parsedRecords) {
        List<Order> orderPlaced = parsedRecords.stream()
                .peek(order -> log.info("Notification: " + order))
                .filter(order -> order.getEventType() == EventType.ORDER_PLACED)
                .collect(Collectors.toList());

        log.info("Placed orders: " + orderPlaced);

        CakeProducerManager cakeProducerManager = new CakeProducerManager();
        if (orderPlaced.size() > 0) {
            cakeProducerManager.handlePlacedOrders(orderPlaced);
        }
    }

    private void notifyDeliveryCompany(List<Order> parsedRecords) {
        List<Order> fulfilledOrders = parsedRecords.stream()
                .peek(order -> log.info("Notification: " + order))
                .filter(order -> order.getEventType() == EventType.ORDER_FULFILLED)
                .collect(Collectors.toList());

        log.info("Fulfilled orders: " + fulfilledOrders);

        DeliveryManager deliveryManager = new DeliveryManager();
        if (fulfilledOrders.size() > 0) {
            deliveryManager.handleFulfilledOrders(fulfilledOrders);
        }
    }

}
