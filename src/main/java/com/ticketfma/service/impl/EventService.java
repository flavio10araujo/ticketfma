package com.ticketfma.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.dto.SeatDTO;
import com.ticketfma.dto.SeatRequest;
import com.ticketfma.exception.EventNotFoundException;
import com.ticketfma.exception.SeatNotExistException;
import com.ticketfma.exception.SeatUnavailableException;
import com.ticketfma.mapper.SeatMapper;
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
    public List<Event> getAllEvents(String sortBy) {
        return repository.getAllEvents(sortBy);
    }

    @Override
    public Optional<SeatDTO> getSeat(String eventId, SeatRequest seatRequest) {
        validateEventExists(eventId);
        Optional<Seat> seat = repository.getSeat(eventId, seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection());
        return seat.map(SeatMapper::toSeatDTO);
    }

    @Override
    public List<SeatDTO> getBestSeats(String eventId, int quantity) {
        validateEventExists(eventId);
        List<Seat> seats = repository.getBestSeats(eventId, quantity);
        return seats.stream()
                .map(SeatMapper::toSeatDTO)
                .collect(Collectors.toList());
    }

    @Override
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
