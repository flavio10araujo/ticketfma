package com.ticketfma.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.dto.SeatRequest;
import com.ticketfma.exception.EventNotFoundException;
import com.ticketfma.exception.SeatNotExistException;
import com.ticketfma.exception.SeatUnavailableException;
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

    public Seat getSeat(String eventId, SeatRequest seatRequest) {
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
