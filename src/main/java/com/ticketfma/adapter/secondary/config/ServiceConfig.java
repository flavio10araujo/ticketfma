package com.ticketfma.adapter.secondary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ticketfma.domain.port.EventRepositoryPort;
import com.ticketfma.domain.port.EventServicePort;
import com.ticketfma.domain.service.EventServiceImpl;

@Configuration
public class ServiceConfig {

    @Bean
    public EventServicePort eventService(EventRepositoryPort eventRepositoryPort) {
        return new EventServiceImpl(eventRepositoryPort);
    }
}
