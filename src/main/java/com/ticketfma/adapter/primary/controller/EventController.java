package com.ticketfma.adapter.primary.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketfma.adapter.primary.dto.SeatRequest;
import com.ticketfma.domain.model.Event;
import com.ticketfma.domain.model.Seat;
import com.ticketfma.domain.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @PostMapping("/v1/events/{eventId}/search-seat")
    @Operation(summary = "Get a specific seat for a specific event.")
    @Parameter(name = "eventId", description = "The ID of the event")
    public ResponseEntity<Seat> getSeat(@PathVariable String eventId, @RequestBody @Valid SeatRequest seatRequest) {
        Optional<Seat> optionalSeat = eventService.getSeat(eventId, seatRequest);
        return optionalSeat.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Validated
    @GetMapping("/v1/events/{eventId}/best-seats")
    @Operation(summary = "Get best seats for a specific event.")
    @Parameter(name = "eventId", description = "The ID of the event")
    public ResponseEntity<List<Seat>> getBestSeats(@PathVariable String eventId, @RequestParam @Min(1) int quantity) {
        List<Seat> bestSeats = eventService.getBestSeats(eventId, quantity);
        return ResponseEntity.ok(bestSeats);
    }

    @PostMapping("/v1/events/{eventId}/reserve-seats")
    @Operation(summary = "Reserve seats for a specific event.")
    @Parameter(name = "eventId", description = "The ID of the event")
    public ResponseEntity<Void> reserveSeats(@PathVariable String eventId, @RequestBody @Valid List<SeatRequest> seatRequests) {
        eventService.reserveSeats(eventId, seatRequests);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
