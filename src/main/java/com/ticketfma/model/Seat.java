package com.ticketfma.model;

import com.ticketfma.model.enums.SeatStatus;

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
