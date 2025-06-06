package com.example.packing_iso_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue validationQueue() {
        return new Queue("iso.validation.request.queue", true);
    }
}
