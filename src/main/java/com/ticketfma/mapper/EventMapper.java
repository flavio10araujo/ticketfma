package com.ticketfma.mapper;

import com.ticketfma.domain.Event;
import com.ticketfma.dto.EventDTO;

public class EventMapper {

    public static EventDTO toEventDTO(Event event) {
        return EventDTO.builder()
                .eventId(event.getEventId())
                .name(event.getName())
                .eventDate(event.getEventDate())
                .build();
    }
}
