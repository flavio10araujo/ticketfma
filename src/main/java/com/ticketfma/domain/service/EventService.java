package com.ticketfma.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ticketfma.adapter.primary.dto.SeatRequest;
import com.ticketfma.domain.exception.EventNotFoundException;
import com.ticketfma.domain.exception.SeatNotExistException;
import com.ticketfma.domain.exception.SeatUnavailableException;
import com.ticketfma.domain.model.Event;
import com.ticketfma.domain.model.Seat;
import com.ticketfma.domain.port.EventRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventService {

    private final EventRepositoryPort repository;

    public List<Event> getAllEvents(Optional<String> sortBy) {
        return repository.getAllEvents(sortBy);
    }

    public Optional<Seat> getSeat(String eventId, SeatRequest seatRequest) {
        validateEventExists(eventId);
        return repository.getSeat(eventId, seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection());
    }

    public List<Seat> getBestSeats(String eventId, int quantity) {
        validateEventExists(eventId);
        return repository.getBestSeats(eventId, quantity);
    }

    public void reserveSeats(String eventId, List<SeatRequest> seatRequests) {
        validateEventExists(eventId);
        validateSeatsExist(eventId, seatRequests);
        validateSeatsAvailable(eventId, seatRequests);
        repository.reserveSeats(eventId, seatRequests);
    }

    private void validateEventExists(String eventId) {
        if (!repository.eventExists(eventId)) {
            log.warn("Event with id {} not found", eventId);
            throw new EventNotFoundException(eventId);
        }
    }

    private void validateSeatsExist(String eventId, List<SeatRequest> seatRequests) {
        for (SeatRequest seatRequest : seatRequests) {
            if (!repository.seatExists(eventId, seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection())) {
                log.warn("Seat '{}' in row '{}' in level '{}' in section '{}' does not exist", seatRequest.getSeatNumber(), seatRequest.getRow(),
                        seatRequest.getLevel(), seatRequest.getSection());
                throw new SeatNotExistException(seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection());
            }
        }
    }

    private void validateSeatsAvailable(String eventId, List<SeatRequest> seatRequests) {
        for (SeatRequest seatRequest : seatRequests) {
            if (!repository.seatAvailable(eventId, seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection())) {
                log.warn("Seat '{}' in row '{}' in level '{}' in section '{}' is already reserved", seatRequest.getSeatNumber(), seatRequest.getRow(),
                        seatRequest.getLevel(), seatRequest.getSection());
                throw new SeatUnavailableException(seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection());
            }
        }
    }
}