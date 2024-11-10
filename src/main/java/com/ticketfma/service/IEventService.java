package com.ticketfma.service;

import java.util.List;
import java.util.Optional;

import com.ticketfma.model.dto.EventDTO;
import com.ticketfma.model.dto.SeatDTO;
import com.ticketfma.model.dto.SeatRequest;

public interface IEventService {
    List<EventDTO> getAllEvents(String sortBy);

    Optional<SeatDTO> getSeat(String eventId, SeatRequest seatRequest);

    List<SeatDTO> getBestSeats(String eventId, int quantity);

    void reserveSeats(String eventId, List<SeatRequest> seatRequests);
}
