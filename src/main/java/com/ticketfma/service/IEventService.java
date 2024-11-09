package com.ticketfma.service;

import java.util.List;
import java.util.Optional;

import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.dto.SeatRequest;

public interface IEventService {
    List<Event> getAllEvents(String sortBy);

    Optional<Seat> getSeat(String eventId, SeatRequest seatRequest);

    List<Seat> getBestSeats(String eventId, int quantity);

    void reserveSeats(String eventId, List<SeatRequest> seatRequests);
}
