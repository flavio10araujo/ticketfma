package com.ticketfma.repository.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.domain.enums.SeatStatus;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Component
public class CsvDataLoader {

    private final List<Event> events = new ArrayList<>();
    private final ConcurrentHashMap<String, List<Seat>> eventSeats = new ConcurrentHashMap<>();

    public void loadCsvData() {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/data.csv"))))) {
            List<String[]> csvData = csvReader.readAll();
            processCsvData(csvData);
        } catch (IOException | CsvException e) {
            log.error("Error reading CSV file.", e);
        }
    }

    private void processCsvData(List<String[]> csvData) {
        for (int i = 1; i < csvData.size(); i++) {
            String[] row = csvData.get(i);
            processCsvRow(row);
        }
    }

    private void processCsvRow(String[] row) {
        String eventId = row[0];
        String seatNumber = row[1];
        String seatRow = row[2];
        String level = row[3];
        String section = row[4];
        String status = row[5];
        LocalDate eventDate = parseEventDate(row[6]);
        int sellRank = Integer.parseInt(row[7]);
        boolean hasUpsells = Boolean.parseBoolean(row[8]);

        Event event = createEvent(eventId, eventDate);
        addEventIfNotExists(event);

        Seat seat = createSeat(seatNumber, seatRow, level, section, status, sellRank, hasUpsells);
        addSeatToEvent(eventId, seat);
    }

    private LocalDate parseEventDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDate.parse(date, formatter);
    }

    private Event createEvent(String eventId, LocalDate eventDate) {
        return Event.builder()
                .eventId(eventId)
                .eventDate(eventDate)
                .name(String.format("Event %03d", Integer.parseInt(eventId)))
                .build();
    }

    private void addEventIfNotExists(Event event) {
        if (events.stream().noneMatch(e -> e.getEventId().equals(event.getEventId()))) {
            events.add(event);
        }
    }

    private Seat createSeat(String seatNumber, String seatRow, String level, String section, String status, int sellRank, boolean hasUpsells) {
        return Seat.builder()
                .seatNumber(seatNumber)
                .row(seatRow)
                .level(level)
                .section(section)
                .status(SeatStatus.valueOf(status))
                .sellRank(sellRank)
                .hasUpsells(hasUpsells)
                .build();
    }

    private void addSeatToEvent(String eventId, Seat seat) {
        eventSeats.computeIfAbsent(eventId, k -> new ArrayList<>()).add(seat);
    }
}
