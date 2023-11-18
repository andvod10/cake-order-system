package com.serverless.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Jacksonized
public class Order implements Serializable {
    UUID orderId;
    private String name;
    private String address;
    private String productId;
    private int quantity;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;
    private UUID fulfillmentId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fulfillmentDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentToDeliveryDate;
    private UUID deliveryCompanyId;
    private Integer orderReview;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deliveryDate;
    private EventType eventType;
}
