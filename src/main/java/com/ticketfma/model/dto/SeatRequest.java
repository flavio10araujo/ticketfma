package com.ticketfma.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SeatRequest {
    @NotBlank
    private String seatNumber;

    @NotBlank
    private String row;

    @NotBlank
    private String level;

    @NotBlank
    private String section;
}
