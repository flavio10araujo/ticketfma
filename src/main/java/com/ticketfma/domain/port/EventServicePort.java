package com.ticketfma.domain.port;

import java.util.List;
import java.util.Optional;

import com.ticketfma.adapter.primary.dto.SeatRequest;
import com.ticketfma.domain.model.Event;
import com.ticketfma.domain.model.Seat;

public interface EventServicePort {

    List<Event> getAllEvents(String sortBy);

    Optional<Seat> getSeat(String eventId, SeatRequest seatRequest);

    List<Seat> getBestSeats(String eventId, int quantity);

    void reserveSeats(String eventId, List<SeatRequest> seatRequests);
}
