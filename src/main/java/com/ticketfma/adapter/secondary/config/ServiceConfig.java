package com.ticketfma.adapter.secondary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ticketfma.domain.port.EventRepositoryPort;
import com.ticketfma.domain.service.EventService;

@Configuration
public class ServiceConfig {

    @Bean
    public EventService eventService(EventRepositoryPort eventRepositoryPort) {
        return new EventService(eventRepositoryPort);
    }
}
