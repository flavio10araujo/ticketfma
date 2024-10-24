package com.ticketfma.domain;

import com.ticketfma.domain.enums.SeatStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Seat {
    private String seatNumber;
    private String row;
    private String level;
    private String section;
    private SeatStatus status;
    private int sellRank;
    private boolean hasUpsells;
}
