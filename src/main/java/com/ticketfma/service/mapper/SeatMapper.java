package com.ticketfma.service.mapper;

import com.ticketfma.model.Seat;
import com.ticketfma.model.dto.SeatDTO;

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
