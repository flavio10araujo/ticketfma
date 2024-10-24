package com.ticketfma.repository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.domain.enums.SeatStatus;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IEventRepository {

    private static final String SORT_BY_NAME = "name";
    private static final String SORT_BY_DATE = "date";
    private final List<Event> events = new ArrayList<>();
    private final Map<String, List<Seat>> eventSeats = new HashMap<>();

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

    public boolean eventExists(String eventId) {
        return eventSeats.containsKey(eventId);
    }

    public List<Seat> getBestSeats(String eventId, int quantity) {
        return eventSeats.get(eventId).stream()
                .filter(seat -> seat.getStatus() == SeatStatus.OPEN)
                .sorted(Comparator.comparingInt(Seat::getSellRank)) // Best rank first.
                .limit(quantity)
                .collect(Collectors.toList());
    }
}
