package com.ticketfma.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.domain.enums.SeatStatus;
import com.ticketfma.exception.EventNotFoundException;
import com.ticketfma.service.EventService;

@ExtendWith(MockitoExtension.class)
public class EventControllerTest {

    private static final String VALID_EVENT_ID = "3001";
    private static final String INVALID_EVENT_ID = "9999";

    @InjectMocks
    private EventController eventController;

    @Mock
    private EventService eventService;

    /* getEvents - BEGIN */
    @Test
    public void givenNoSort_whenGetEvents_thenReturnAllEvents() {
        List<Event> events = getEvents();
        when(eventService.getAllEvents(Optional.empty())).thenReturn(events);

        ResponseEntity<List<Event>> response = eventController.getEvents(Optional.empty());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(events, response.getBody());
        verify(eventService).getAllEvents(Optional.empty());
    }

    @Test
    public void givenSortByName_whenGetEvents_thenReturnAllEventsSortedByName() {
        List<Event> events = getEvents();
        when(eventService.getAllEvents(Optional.of("name"))).thenReturn(events);

        ResponseEntity<List<Event>> response = eventController.getEvents(Optional.of("name"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(events, response.getBody());
        verify(eventService).getAllEvents(Optional.of("name"));
    }

    @Test
    public void givenSortByDate_whenGetEvents_thenReturnAllEventsSortedByDate() {
        List<Event> events = getEvents();
        when(eventService.getAllEvents(Optional.of("date"))).thenReturn(events);

        ResponseEntity<List<Event>> response = eventController.getEvents(Optional.of("date"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(events, response.getBody());
        verify(eventService).getAllEvents(Optional.of("date"));
    }
    /* getEvents - END */

    /* getBestSeats - BEGIN */
    @Test
    public void givenValidEventIdAndQuantity_whenGetBestSeats_thenReturnBestSeats() {
        List<Seat> seats = getSeats();
        when(eventService.getBestSeats(VALID_EVENT_ID, 5)).thenReturn(seats);

        ResponseEntity<List<Seat>> response = eventController.getBestSeats(VALID_EVENT_ID, 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(seats, response.getBody());
        verify(eventService).getBestSeats(VALID_EVENT_ID, 5);
    }

    @Test
    public void givenInvalidEventId_whenGetBestSeats_thenReturnNotFound() {
        when(eventService.getBestSeats(INVALID_EVENT_ID, 5)).thenThrow(new EventNotFoundException(INVALID_EVENT_ID));

        assertThrows(EventNotFoundException.class, () -> {
            eventController.getBestSeats(INVALID_EVENT_ID, 5);
        });

        verify(eventService).getBestSeats(INVALID_EVENT_ID, 5);
    }
    /* getBestSeats - END */

    /* stubs - BEGIN */
    private List<Event> getEvents() {
        return List.of(
                Event.builder().eventId("1").eventDate(LocalDate.parse("2022-01-01")).name("Event 001").build(),
                Event.builder().eventId("2").eventDate(LocalDate.parse("2022-01-02")).name("Event 002").build(),
                Event.builder().eventId("3").eventDate(LocalDate.parse("2022-01-03")).name("Event 003").build()
        );
    }

    private List<Seat> getSeats() {
        return List.of(
                Seat.builder().seatNumber("2").row("17").level("b").section("E").status(SeatStatus.OPEN).sellRank(2).hasUpsells(false).build(),
                Seat.builder().seatNumber("3").row("35").level("t").section("K").status(SeatStatus.OPEN).sellRank(3).hasUpsells(false).build(),
                Seat.builder().seatNumber("3").row("30").level("z").section("f").status(SeatStatus.OPEN).sellRank(3).hasUpsells(false).build()

        );
    }
    /* stubs - END */
}
