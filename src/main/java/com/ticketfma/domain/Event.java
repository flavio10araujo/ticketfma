package com.ticketfma.domain;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    private String eventId;
    private String name;
    private LocalDate eventDate;
}
