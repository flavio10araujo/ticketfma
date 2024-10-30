package com.ticketfma.domain.exception;

import java.io.Serial;

public class EventNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EventNotFoundException(String id) {
        super(String.format("Event '%s' not found.", id));
    }
}
