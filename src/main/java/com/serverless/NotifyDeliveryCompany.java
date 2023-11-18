package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.apigateway.ApiGatewayResponse;
import lombok.extern.log4j.Log4j;

import java.util.Map;

@Log4j
public class NotifyDeliveryCompany implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> event, Context context) {
        log.info("Let's imagine HTTP call here. Event: " + event);

        return null;
    }

}
