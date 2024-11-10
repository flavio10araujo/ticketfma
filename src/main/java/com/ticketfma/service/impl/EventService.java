package com.ticketfma.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ticketfma.exception.EventNotFoundException;
import com.ticketfma.exception.SeatNotFoundException;
import com.ticketfma.exception.SeatUnavailableException;
import com.ticketfma.mapper.EventMapper;
import com.ticketfma.mapper.SeatMapper;
import com.ticketfma.model.Event;
import com.ticketfma.model.Seat;
import com.ticketfma.model.dto.EventDTO;
import com.ticketfma.model.dto.SeatDTO;
import com.ticketfma.model.dto.SeatRequest;
import com.ticketfma.repository.IEventRepository;
import com.ticketfma.service.IEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventService implements IEventService {

    private final IEventRepository repository;

    @Override
    public List<EventDTO> getAllEvents(String sortBy) {
        List<Event> events = repository.getAllEvents(sortBy);
        return events.stream()
                .map(EventMapper::toEventDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SeatDTO> getSeat(String eventId, SeatRequest seatRequest) {
        if (!isEventExists(eventId)) {
            throw new EventNotFoundException(eventId);
        }

        Optional<Seat> seat = repository.getSeat(eventId, seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection());
        return seat.map(SeatMapper::toSeatDTO);
    }

    @Override
    public List<SeatDTO> getBestSeats(String eventId, int quantity) {
        if (!isEventExists(eventId)) {
            throw new EventNotFoundException(eventId);
        }

        List<Seat> seats = repository.getBestSeats(eventId, quantity);
        return seats.stream()
                .map(SeatMapper::toSeatDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void reserveSeats(String eventId, List<SeatRequest> seatRequests) {
        if (!isEventExists(eventId)) {
            throw new EventNotFoundException(eventId);
        }

        for (SeatRequest seatRequest : seatRequests) {
            if (!isSeatRequestExists(eventId, seatRequest)) {
                throw new SeatNotFoundException(seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection());
            }

            if (!isSeatRequestAvailable(eventId, seatRequest)) {
                throw new SeatUnavailableException(seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection());
            }
        }

        repository.reserveSeats(eventId, seatRequests);
    }

    private boolean isEventExists(String eventId) {
        if (!repository.eventExists(eventId)) {
            log.warn("Event with id {} not found", eventId);
            return false;
        }

        return true;
    }

    private boolean isSeatRequestExists(String eventId, SeatRequest seatRequest) {
        if (!repository.seatExists(eventId, seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection())) {
            log.warn("Seat '{}' in row '{}' in level '{}' in section '{}' does not exist", seatRequest.getSeatNumber(), seatRequest.getRow(),
                    seatRequest.getLevel(), seatRequest.getSection());
            return false;
        }

        return true;
    }

    private boolean isSeatRequestAvailable(String eventId, SeatRequest seatRequest) {
        if (!repository.seatAvailable(eventId, seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection())) {
            log.warn("Seat '{}' in row '{}' in level '{}' in section '{}' is already reserved", seatRequest.getSeatNumber(), seatRequest.getRow(),
                    seatRequest.getLevel(), seatRequest.getSection());
            return false;
        }

        return true;
    }
}
