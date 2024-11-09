package com.ticketfma.mapper;

import com.ticketfma.domain.Seat;
import com.ticketfma.dto.SeatDTO;

public class SeatMapper {

    public static SeatDTO toSeatDTO(Seat seat) {
        return SeatDTO.builder()
                .seatNumber(seat.getSeatNumber())
                .row(seat.getRow())
                .level(seat.getLevel())
                .section(seat.getSection())
                .status(seat.getStatus())
                .build();
    }
}
