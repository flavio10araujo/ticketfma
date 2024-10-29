package com.ticketfma.domain.port;

import java.util.List;
import java.util.Optional;

import com.ticketfma.adapter.primary.dto.SeatRequest;
import com.ticketfma.domain.model.Event;
import com.ticketfma.domain.model.Seat;

public interface EventRepositoryPort {

    boolean eventExists(String eventId);

    boolean seatExists(String eventId, String seatNumber, String row, String level, String section);

    boolean seatAvailable(String eventId, String seatNumber, String row, String level, String section);

    List<Event> getAllEvents(Optional<String> sortBy);

    Optional<Seat> getSeat(String eventId, String seatNumber, String row, String level, String section);

    List<Seat> getBestSeats(String eventId, int quantity);

    void reserveSeats(String eventId, List<SeatRequest> seatRequests);
}
