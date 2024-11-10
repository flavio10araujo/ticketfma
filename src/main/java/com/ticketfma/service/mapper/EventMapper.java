package com.ticketfma.service.mapper;

import com.ticketfma.model.Event;
import com.ticketfma.model.dto.EventDTO;

public class EventMapper {

    public static EventDTO toEventDTO(Event event) {
        return EventDTO.builder()
                .eventId(event.getEventId())
                .name(event.getName())
                .eventDate(event.getEventDate())
                .build();
    }
}
