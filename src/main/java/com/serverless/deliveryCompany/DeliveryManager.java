package com.serverless.deliveryCompany;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.serverless.order.Order;
import com.serverless.order.OrderManager;
import lombok.extern.log4j.Log4j;

import java.util.List;

@Log4j
public class DeliveryManager {

    private final OrderManager orderManager;
    private final String DELIVERY_COMPANY_QUEUE;
    private final AmazonSQS SQS_CLIENT;

    public DeliveryManager() {
        orderManager = new OrderManager();
        DELIVERY_COMPANY_QUEUE = System.getenv("deliveryCompanyQueue");
        String region = System.getenv("region");
        SQS_CLIENT = AmazonSQSClient.builder()
                .withRegion(region)
                .build();
    }

    public void handleFulfilledOrders(List<Order> fulfilledOrders) {
        fulfilledOrders.stream()
                .map(orderManager::updateOrderForDeliveryAndPersist)
                .forEach(this::notifyDeliveryCompany);
    }

    private void notifyDeliveryCompany(Order order) {
        SendMessageResult sendMessageResult = SQS_CLIENT.sendMessage(
                new SendMessageRequest()
                        .withQueueUrl(DELIVERY_COMPANY_QUEUE)
                        .withMessageBody(order.toString())
        );
        log.info("Message sent: " + sendMessageResult.getMessageId());
    }

}
