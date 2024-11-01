package com.ticketfma.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.domain.enums.SeatStatus;
import com.ticketfma.dto.SeatRequest;
import com.ticketfma.exception.SeatNotExistException;
import com.ticketfma.exception.SeatUnavailableException;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class EventRepository {

    private static final String SORT_BY_NAME = "name";
    private static final String SORT_BY_DATE = "date";
    private final CsvDataLoader csvDataLoader;
    private final List<Event> events = new ArrayList<>();
    private final ConcurrentHashMap<String, List<Seat>> eventSeats = new ConcurrentHashMap<>();
    @Getter private final ConcurrentHashMap<String, Lock> eventLocks = new ConcurrentHashMap<>();
    @Getter private final ConcurrentHashMap<String, AtomicInteger> lockCounts = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadCsvData() {
        csvDataLoader.loadCsvData();
        events.addAll(csvDataLoader.getEvents());
        eventSeats.putAll(csvDataLoader.getEventSeats());
    }

    public boolean eventExists(String eventId) {
        return eventSeats.containsKey(eventId);
    }

    public boolean seatExists(String eventId, String seatNumber, String row, String level, String section) {
        return eventSeats.get(eventId).stream()
                .anyMatch(seat -> seat.getSeatNumber().equals(seatNumber) && seat.getRow().equals(row) && seat.getLevel().equals(level) && seat.getSection()
                        .equals(section));
    }

    public boolean seatAvailable(String eventId, String seatNumber, String row, String level, String section) {
        return eventSeats.get(eventId).stream()
                .anyMatch(seat -> seat.getSeatNumber().equals(seatNumber) && seat.getRow().equals(row) && seat.getLevel().equals(level) && seat.getSection()
                        .equals(section) && seat.getStatus() == SeatStatus.OPEN);
    }

    public List<Event> getAllEvents(Optional<String> sortBy) {
        return events.stream()
                .sorted((e1, e2) -> {
                    if (sortBy.isPresent()) {
                        if (sortBy.get().equals(SORT_BY_NAME)) {
                            return e1.getName().compareTo(e2.getName());
                        } else if (sortBy.get().equals(SORT_BY_DATE)) {
                            return e1.getEventDate().compareTo(e2.getEventDate());
                        }
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }

    public Optional<Seat> getSeat(String eventId, String seatNumber, String row, String level, String section) {
        return eventSeats.get(eventId).stream()
                .filter(seat -> seat.getSeatNumber().equals(seatNumber) && seat.getRow().equals(row) && seat.getLevel().equals(level) && seat.getSection()
                        .equals(section))
                .findFirst();
    }

    public List<Seat> getBestSeats(String eventId, int quantity) {
        return eventSeats.get(eventId).stream()
                .filter(seat -> seat.getStatus() == SeatStatus.OPEN)
                .sorted(Comparator.comparingInt(Seat::getSellRank)) // Best rank first.
                .limit(quantity)
                .collect(Collectors.toList());
    }

    public void reserveSeats(String eventId, List<SeatRequest> seatRequests) {
        Lock eventLock = eventLocks.computeIfAbsent(eventId, id -> new ReentrantLock());
        AtomicInteger lockCount = lockCounts.computeIfAbsent(eventId, id -> new AtomicInteger(0));
        lockCount.incrementAndGet();
        eventLock.lock();

        try {
            for (SeatRequest seatRequest : seatRequests) {
                if (!seatAvailable(eventId, seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection())) {
                    log.warn("Seat '{}' in row '{}' in level '{}' in section '{}' is already reserved", seatRequest.getSeatNumber(), seatRequest.getRow(),
                            seatRequest.getLevel(), seatRequest.getSection());
                    throw new SeatUnavailableException(seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection());
                }

                Optional<Seat> optionalSeat = getSeat(eventId, seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(),
                        seatRequest.getSection());
                if (optionalSeat.isPresent()) {
                    Seat seat = optionalSeat.get();
                    seat.setStatus(SeatStatus.HOLD);
                } else {
                    throw new SeatNotExistException(seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection());
                }
            }
        } finally {
            eventLock.unlock();

            if (lockCount.decrementAndGet() == 0) {
                eventLocks.remove(eventId, eventLock);
                lockCounts.remove(eventId, lockCount);
            }
        }
    }
}
