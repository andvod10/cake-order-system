package com.serverless.order;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serverless.apigateway.OrderRequest;
import lombok.extern.log4j.Log4j;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Log4j
public class OrderManager {

    private final ObjectMapper objectMapper;
    private final Table table;
    private final String STREAM_NAME;

    public OrderManager() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        String TABLE_NAME = System.getenv("orderTableName");
        STREAM_NAME = System.getenv("orderStreamName");

        DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
        table = dynamoDB.getTable(TABLE_NAME);
    }

    public void placeNewOrder(Order order) {
        try {
            saveOrder(order);
            PutRecordResult savedInStream = placeOrderStream(order);
            log.info("Kinesis sequence number: " + savedInStream.getSequenceNumber());
        } catch (JsonProcessingException e) {
            log.error("This is error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Order fulfillOrder(OrderRequest orderRequest) {
        Item item = table.getItem("orderId", orderRequest.getOrderId().toString());
        Order order;
        try {
            order = objectMapper.readValue(item.toJSON(), Order.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        order.setFulfillmentId(orderRequest.getFulfillmentId());
        order.setFulfillmentDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        order.setEventType(EventType.ORDER_FULFILLED);

        log.info("Fulfilled order: " + order);

        try {
            saveOrder(order);
            placeOrderStream(order);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return order;
    }

    public Order deliveryOrder(OrderRequest orderRequest) {
        Item item = table.getItem("orderId", orderRequest.getOrderId().toString());
        Order order;
        try {
            order = objectMapper.readValue(item.toJSON(), Order.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        order.setDeliveryCompanyId(orderRequest.getDeliveryCompanyId());
        order.setOrderReview(orderRequest.getOrderReview());
        order.setDeliveryDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        order.setEventType(EventType.ORDER_DELIVERED);

        log.info("Delivered Order: " + order);

        try {
            saveOrder(order);
            placeOrderStream(order);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return order;
    }

    public Order updateOrderForDeliveryAndPersist(Order order) {
        order.setSentToDeliveryDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        try {
            return saveOrder(order);
        } catch (JsonProcessingException e) {
            log.error("This is error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Order saveOrder(Order order) throws JsonProcessingException {
        PutItemOutcome savedOrder = table.putItem(Item.fromJSON(objectMapper.writeValueAsString(order)));
        if (savedOrder.getPutItemResult().getSdkHttpMetadata().getHttpStatusCode() != 200) {
            throw new RuntimeException("Saving into dynamo DB was failed. Response metadata id: " +
                    savedOrder.getPutItemResult().getSdkResponseMetadata().getRequestId());
        }
        return order;
    }

    private PutRecordResult placeOrderStream(Order order) throws JsonProcessingException {
        AmazonKinesis kinesisClient = AmazonKinesisClientBuilder.defaultClient();
        PutRecordRequest putRecordRequest = new PutRecordRequest()
                .withStreamName(STREAM_NAME)
                .withData(ByteBuffer.wrap(objectMapper.writeValueAsBytes(order)))
                .withPartitionKey(order.getOrderId().toString());

        return kinesisClient.putRecord(putRecordRequest);
    }

}
