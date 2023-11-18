package com.serverless.cakeProducer;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;
import com.serverless.order.Order;
import lombok.extern.log4j.Log4j;

import java.util.List;

@Log4j
public class CakeProducerManager {

    private final String CAKE_PRODUCER_EMAIL;
    private final String ORDERING_SYSTEM_EMAIL;
    private final AmazonSimpleEmailService SES_CLIENT;

    public CakeProducerManager() {
        CAKE_PRODUCER_EMAIL = System.getenv("cakeProducerEmail");
        ORDERING_SYSTEM_EMAIL = System.getenv("orderingSystemEmail");
        String region = System.getenv("region");
        SES_CLIENT = AmazonSimpleEmailServiceClient.builder()
                .withRegion(region)
                .build();
    }

    public void handlePlacedOrders(List<Order> placedOrders) {
        placedOrders.forEach(this::notifyCakeProducerByEmail);
    }

    private void notifyCakeProducerByEmail(Order order) {
        SendEmailResult sendEmailResult = SES_CLIENT.sendEmail(new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(CAKE_PRODUCER_EMAIL))
                .withMessage(new Message()
                        .withBody(new Body().withText(new Content(order.toString())))
                        .withSubject(new Content("New Cake order"))
                )
                .withSource(ORDERING_SYSTEM_EMAIL)
        );

        log.info("Email sent: " + sendEmailResult.getMessageId());
    }

}
