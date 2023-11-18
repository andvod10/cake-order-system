package com.serverless.apigateway;

import com.serverless.order.EventType;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
@Jacksonized
public class OrderRequest implements Serializable {
    UUID orderId;
    String name;
    String address;
    String productId;
    Integer quantity;
    LocalDateTime orderDate;
    UUID fulfillmentId;
    LocalDateTime fulfillmentDate;
    LocalDateTime sentToDeliveryDate;
    UUID deliveryCompanyId;
    Integer orderReview;
    LocalDateTime deliveryDate;
    EventType eventType;
}
