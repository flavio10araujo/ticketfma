package com.ticketfma.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EventNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EventNotFoundException(String id) {
        super(String.format("Event '%s' not found.", id));
    }
}
