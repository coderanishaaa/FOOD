package com.fooddelivery.payment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EurekaConfig {

    private static final Logger log = LoggerFactory.getLogger(EurekaConfig.class);

    @Bean
    public EurekaClientConfigBean eurekaClientConfigBean() {
        EurekaClientConfigBean config = new EurekaClientConfigBean();
        log.info("Eureka client configuration initialized");
        return config;
    }
}
