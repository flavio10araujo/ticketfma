package com.ticketfma.adapter.secondary.persistence;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.ticketfma.adapter.primary.dto.SeatRequest;
import com.ticketfma.domain.exception.SeatNotExistException;
import com.ticketfma.domain.exception.SeatUnavailableException;
import com.ticketfma.domain.model.Event;
import com.ticketfma.domain.model.Seat;
import com.ticketfma.domain.model.enums.SeatStatus;
import com.ticketfma.domain.port.EventRepositoryPort;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class EventRepositoryCSV implements EventRepositoryPort {

    private static final String SORT_BY_NAME = "name";
    private static final String SORT_BY_DATE = "date";
    private final List<Event> events = new ArrayList<>();
    private final ConcurrentHashMap<String, List<Seat>> eventSeats = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Lock> eventLocks = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadCsvData() {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/data.csv"))))) {
            List<String[]> csvData = csvReader.readAll();

            // Process each row in the CSV (skipping the header).
            for (int i = 1; i < csvData.size(); i++) {
                String[] row = csvData.get(i);
                String eventId = row[0];
                String seatNumber = row[1];
                String seatRow = row[2];
                String level = row[3];
                String section = row[4];
                String status = row[5];
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDate eventDate = LocalDate.parse(row[6], formatter);
                int sellRank = Integer.parseInt(row[7]);
                boolean hasUpsells = Boolean.parseBoolean(row[8]);

                Event event = Event.builder()
                        .eventId(eventId)
                        .eventDate(eventDate)
                        .name(String.format("Event %03d", Integer.parseInt(eventId))) // Because we don't have the event name in the CSV.
                        .build();

                // Add event to the list if not already present.
                if (events.stream().noneMatch(e -> e.getEventId().equals(eventId))) {
                    events.add(event);
                }

                // Create seat object
                Seat seat = Seat.builder()
                        .seatNumber(seatNumber)
                        .row(seatRow)
                        .level(level)
                        .section(section)
                        .status(SeatStatus.valueOf(status))
                        .sellRank(sellRank)
                        .hasUpsells(hasUpsells)
                        .build();

                // Add seat to the event's seat list.
                eventSeats.computeIfAbsent(eventId, k -> new ArrayList<>()).add(seat);
            }

        } catch (IOException | CsvException e) {
            log.error("Error reading CSV file.", e);
        }
    }

    @Override
    public boolean eventExists(String eventId) {
        return eventSeats.containsKey(eventId);
    }

    @Override
    public boolean seatExists(String eventId, String seatNumber, String row, String level, String section) {
        return eventSeats.get(eventId).stream()
                .anyMatch(seat -> seat.getSeatNumber().equals(seatNumber) && seat.getRow().equals(row) && seat.getLevel().equals(level) && seat.getSection()
                        .equals(section));
    }

    @Override
    public boolean seatAvailable(String eventId, String seatNumber, String row, String level, String section) {
        return eventSeats.get(eventId).stream()
                .anyMatch(seat -> seat.getSeatNumber().equals(seatNumber) && seat.getRow().equals(row) && seat.getLevel().equals(level) && seat.getSection()
                        .equals(section) && seat.getStatus() == SeatStatus.OPEN);
    }

    @Override
    public List<Event> getAllEvents(String sortBy) {
        return events.stream()
                .sorted((e1, e2) -> {
                    if (sortBy != null) {
                        if (SORT_BY_NAME.equalsIgnoreCase(sortBy)) {
                            return e1.getName().compareTo(e2.getName());
                        } else if (SORT_BY_DATE.equalsIgnoreCase(sortBy)) {
                            return e1.getEventDate().compareTo(e2.getEventDate());
                        }
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Seat> getSeat(String eventId, String seatNumber, String row, String level, String section) {
        return eventSeats.get(eventId).stream()
                .filter(seat -> seat.getSeatNumber().equals(seatNumber) && seat.getRow().equals(row) && seat.getLevel().equals(level) && seat.getSection()
                        .equals(section))
                .findFirst();
    }

    @Override
    public List<Seat> getBestSeats(String eventId, int quantity) {
        return eventSeats.get(eventId).stream()
                .filter(seat -> seat.getStatus() == SeatStatus.OPEN)
                .sorted(Comparator.comparingInt(Seat::getSellRank)) // Best rank first.
                .limit(quantity)
                .collect(Collectors.toList());
    }

    @Override
    public void reserveSeats(String eventId, List<SeatRequest> seatRequests) {
        Lock eventLock = eventLocks.computeIfAbsent(eventId, id -> new ReentrantLock());
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
        }
    }
}
