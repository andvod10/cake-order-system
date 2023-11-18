package com.serverless.mapper;

import com.serverless.apigateway.OrderRequest;
import com.serverless.order.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface OrderMapper {

    OrderMapper INSTANCE = new OrderMapperImpl();

    @Mapping(target = "orderId", defaultExpression = "java(generateUuid())")
    @Mapping(target = "eventType", constant = "ORDER_PLACED")
    Order toOrderPlaced(OrderRequest orderRequest);

    default UUID generateUuid() {
        return UUID.randomUUID();
    }

}
