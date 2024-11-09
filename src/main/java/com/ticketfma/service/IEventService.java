package com.ticketfma.service;

import java.util.List;
import java.util.Optional;

import com.ticketfma.domain.Event;
import com.ticketfma.dto.SeatDTO;
import com.ticketfma.dto.SeatRequest;

public interface IEventService {
    List<Event> getAllEvents(String sortBy);

    Optional<SeatDTO> getSeat(String eventId, SeatRequest seatRequest);

    List<SeatDTO> getBestSeats(String eventId, int quantity);

    void reserveSeats(String eventId, List<SeatRequest> seatRequests);
}
