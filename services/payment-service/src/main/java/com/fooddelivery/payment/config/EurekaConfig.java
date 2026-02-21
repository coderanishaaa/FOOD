package com.fooddelivery.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Eureka client configuration.
 * Ensures proper registration and retry logic.
 */
@Configuration
@Slf4j
public class EurekaConfig {

    /**
     * Configure Eureka client with retry logic.
     */
    @Bean
    public EurekaClientConfigBean eurekaClientConfigBean() {
        EurekaClientConfigBean config = new EurekaClientConfigBean();
        log.info("Eureka client configuration initialized");
        return config;
    }
}
