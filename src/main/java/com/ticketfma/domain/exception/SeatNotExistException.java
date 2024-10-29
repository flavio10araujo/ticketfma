package com.ticketfma.domain.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SeatNotExistException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SeatNotExistException(String seatNumber, String row, String level, String section) {
        super(String.format("Seat '%s' in row '%s' in level '%s' in section '%s' does not exist.", seatNumber, row, level, section));
    }
}
