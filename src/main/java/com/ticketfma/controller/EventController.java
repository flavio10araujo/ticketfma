package com.ticketfma.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "Event controller")
public class EventController {

    private final EventService eventService;

    @GetMapping("/v1/events")
    @Operation(summary = "Get all available events with optional sorting by event name or event date.")
    public ResponseEntity<List<Event>> getEvents(@RequestParam Optional<String> sort) {
        List<Event> events = eventService.getAllEvents(sort);
        return ResponseEntity.ok(events);
    }

    @Validated
    @GetMapping("/v1/events/{eventId}/best-seats")
    @Operation(summary = "Get best seats for a specific event.")
    @Parameter(name = "eventId", description = "The ID of the event")
    public ResponseEntity<List<Seat>> getBestSeats(@PathVariable String eventId, @RequestParam @Min(1) int quantity) {
        try {
            List<Seat> bestSeats = eventService.getBestSeats(eventId, quantity);
            return ResponseEntity.ok(bestSeats);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
