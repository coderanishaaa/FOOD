package com.fooddelivery.payment.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Custom Feign error decoder to handle service communication errors gracefully.
 */
@Component
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

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
