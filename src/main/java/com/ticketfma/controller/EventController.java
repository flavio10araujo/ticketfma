package com.ticketfma.controller;

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

import com.ticketfma.model.dto.EventDTO;
import com.ticketfma.model.dto.SeatDTO;
import com.ticketfma.model.dto.SeatRequest;
import com.ticketfma.service.IEventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "Event controller")
public class EventController {

    private final IEventService eventService;

    @GetMapping("/v1/events")
    @Operation(summary = "Get all available events with optional sorting by event name or event date.")
    public ResponseEntity<List<EventDTO>> getEvents(@RequestParam Optional<String> sort) {
        List<EventDTO> events = eventService.getAllEvents(sort.orElse(null));
        return ResponseEntity.ok(events);
    }

    @PostMapping("/v1/events/{eventId}/search-seat")
    @Operation(summary = "Get a specific seat for a specific event.")
    @Parameter(name = "eventId", description = "The ID of the event")
    public ResponseEntity<SeatDTO> getSeat(@PathVariable String eventId, @RequestBody @Valid SeatRequest seatRequest) {
        Optional<SeatDTO> optionalSeat = eventService.getSeat(eventId, seatRequest);
        return optionalSeat.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Validated
    @GetMapping("/v1/events/{eventId}/best-seats")
    @Operation(summary = "Get best seats for a specific event.")
    @Parameter(name = "eventId", description = "The ID of the event")
    public ResponseEntity<List<SeatDTO>> getBestSeats(@PathVariable String eventId, @RequestParam @Min(1) int quantity) {
        List<SeatDTO> bestSeats = eventService.getBestSeats(eventId, quantity);
        return ResponseEntity.ok(bestSeats);
    }

    @PostMapping("/v1/events/{eventId}/reserve-seats")
    @Operation(summary = "Reserve seats for a specific event.")
    @Parameter(name = "eventId", description = "The ID of the event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Seats reserved"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "400", description = "Seat does not exist"),
            @ApiResponse(responseCode = "409", description = "Seat is unavailable")
    })
    public ResponseEntity<Void> reserveSeats(@PathVariable String eventId, @RequestBody @Valid List<SeatRequest> seatRequests) {
        eventService.reserveSeats(eventId, seatRequests);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
