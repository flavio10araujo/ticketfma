package com.ticketfma.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SeatUnavailableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SeatUnavailableException(String seatNumber, String row, String level, String section) {
        super(String.format("Seat '%s' in row '%s' in level '%s' in section '%s' is not available.", seatNumber, row, level, section));
    }
}
