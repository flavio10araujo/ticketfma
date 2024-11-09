package com.ticketfma.dto;

import com.ticketfma.domain.enums.SeatStatus;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SeatDTO {
    private String seatNumber;
    private String row;
    private String level;
    private String section;
    private SeatStatus status;
}