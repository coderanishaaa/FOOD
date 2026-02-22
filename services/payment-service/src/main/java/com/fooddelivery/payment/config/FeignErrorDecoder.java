package com.fooddelivery.payment.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private static final Logger log = LoggerFactory.getLogger(FeignErrorDecoder.class);

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign client error: methodKey={}, status={}, reason={}",
                methodKey, response.status(), response.reason());

        if (response.status() == 404) {
            return new RuntimeException("Order service returned 404 - Order not found");
        }

        if (response.status() >= 500) {
            return new RuntimeException("Order service error: " + response.status() + " " + response.reason());
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }
}
