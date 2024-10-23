package com.ticketfma.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.repository.IEventRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EventService {

    private final IEventRepository repository;

    public List<Event> getAllEvents(Optional<String> sortBy) {
        return repository.getAllEvents(sortBy);
    }

    public List<Seat> getBestSeats(String eventId, int quantity) {
        if (!repository.eventExists(eventId)) {
            throw new NoSuchElementException("Event not found.");
        }

        return repository.getBestSeats(eventId, quantity);
    }
}
