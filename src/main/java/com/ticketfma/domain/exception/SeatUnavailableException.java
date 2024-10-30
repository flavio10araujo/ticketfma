package com.ticketfma.domain.exception;

import java.io.Serial;

public class SeatUnavailableException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SeatUnavailableException(String seatNumber, String row, String level, String section) {
        super(String.format("Seat '%s' in row '%s' in level '%s' in section '%s' is not available.", seatNumber, row, level, section));
    }
}
