package com.ticketfma.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.dto.SeatReservationRequest;
import com.ticketfma.exception.EventNotFoundException;
import com.ticketfma.repository.IEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventService {

    private final IEventRepository repository;

    public List<Event> getAllEvents(Optional<String> sortBy) {
        return repository.getAllEvents(sortBy);
    }

    public List<Seat> getBestSeats(String eventId, int quantity) {
        validateEventExists(eventId);
        return repository.getBestSeats(eventId, quantity);
    }

    public void reserveSeats(String eventId, List<SeatReservationRequest> seatRequests) {
        validateEventExists(eventId);
    }

    private void validateEventExists(String eventId) {
        if (!repository.eventExists(eventId)) {
            log.warn("Event with id {} not found", eventId);
            throw new EventNotFoundException(eventId);
        }
    }
}
