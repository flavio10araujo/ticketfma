package com.ticketfma.model.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventDTO {
    private String eventId;
    private String name;
    private LocalDate eventDate;
}
