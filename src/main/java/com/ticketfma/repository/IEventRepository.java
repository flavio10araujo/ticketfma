package com.ticketfma.repository;

import java.util.List;
import java.util.Optional;

import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.dto.SeatRequest;

public interface IEventRepository {
    boolean eventExists(String eventId);

    boolean seatExists(String eventId, String seatNumber, String row, String level, String section);

    boolean seatAvailable(String eventId, String seatNumber, String row, String level, String section);

    List<Event> getAllEvents(String sortBy);

    Optional<Seat> getSeat(String eventId, String seatNumber, String row, String level, String section);

    List<Seat> getBestSeats(String eventId, int quantity);

    void reserveSeats(String eventId, List<SeatRequest> seatRequests);
}