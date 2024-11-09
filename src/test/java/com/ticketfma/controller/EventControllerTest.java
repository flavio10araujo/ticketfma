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

import com.ticketfma.domain.enums.SeatStatus;
import com.ticketfma.dto.EventDTO;
import com.ticketfma.dto.SeatDTO;
import com.ticketfma.dto.SeatRequest;
import com.ticketfma.exception.EventNotFoundException;
import com.ticketfma.service.impl.EventService;

@ExtendWith(MockitoExtension.class)
public class EventControllerTest {

    private static final String VALID_EVENT_ID = "3001";
    private static final String INVALID_EVENT_ID = "9999";
    private static final String SORT_BY_NAME = "name";
    private static final String SORT_BY_DATE = "date";

    @InjectMocks
    private EventController eventController;

    @Mock
    private EventService eventService;

    /* getEvents - BEGIN */
    @Test
    public void givenNoSort_whenGetEvents_thenReturnAllEvents() {
        List<EventDTO> events = getEvents();
        when(eventService.getAllEvents(null)).thenReturn(events);

        ResponseEntity<List<EventDTO>> response = eventController.getEvents(Optional.empty());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(events, response.getBody());
        verify(eventService).getAllEvents(null);
    }

    @Test
    public void givenSortByName_whenGetEvents_thenReturnAllEventsSortedByName() {
        List<EventDTO> events = getEvents();
        when(eventService.getAllEvents(SORT_BY_NAME)).thenReturn(events);

        ResponseEntity<List<EventDTO>> response = eventController.getEvents(Optional.of(SORT_BY_NAME));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(events, response.getBody());
        verify(eventService).getAllEvents(SORT_BY_NAME);
    }

    @Test
    public void givenSortByDate_whenGetEvents_thenReturnAllEventsSortedByDate() {
        List<EventDTO> events = getEvents();
        when(eventService.getAllEvents(SORT_BY_DATE)).thenReturn(events);

        ResponseEntity<List<EventDTO>> response = eventController.getEvents(Optional.of(SORT_BY_DATE));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(events, response.getBody());
        verify(eventService).getAllEvents(SORT_BY_DATE);
    }
    /* getEvents - END */

    /* getSeat - BEGIN */
    @Test
    public void givenValidEventIdAndValidSeatRequest_whenGetSeat_thenReturnSeat() {
        SeatRequest seatRequest = getSeatRequest();
        SeatDTO seat = getSeats().getFirst();
        when(eventService.getSeat(VALID_EVENT_ID, seatRequest)).thenReturn(Optional.ofNullable(seat));

        ResponseEntity<SeatDTO> response = eventController.getSeat(VALID_EVENT_ID, seatRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(seat, response.getBody());
        verify(eventService).getSeat(VALID_EVENT_ID, seatRequest);
    }

    @Test
    public void givenInvalidEventId_whenGetSeat_thenReturnNotFound() {
        SeatRequest seatRequest = getSeatRequest();
        when(eventService.getSeat(INVALID_EVENT_ID, seatRequest)).thenThrow(new EventNotFoundException(INVALID_EVENT_ID));

        assertThrows(EventNotFoundException.class, () -> {
            eventController.getSeat(INVALID_EVENT_ID, seatRequest);
        });

        verify(eventService).getSeat(INVALID_EVENT_ID, seatRequest);
    }

    @Test
    public void givenValidEventIdAndInvalidSeatRequest_whenGetSeat_thenReturnNotFound() {
        SeatRequest seatRequest = getSeatRequest();
        when(eventService.getSeat(VALID_EVENT_ID, seatRequest)).thenReturn(Optional.empty());

        ResponseEntity<SeatDTO> response = eventController.getSeat(VALID_EVENT_ID, seatRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(eventService).getSeat(VALID_EVENT_ID, seatRequest);
    }
    /* getSeat - END */

    /* getBestSeats - BEGIN */
    @Test
    public void givenValidEventIdAndValidQuantity_whenGetBestSeats_thenReturnBestSeats() {
        List<SeatDTO> seats = getSeats();
        when(eventService.getBestSeats(VALID_EVENT_ID, 5)).thenReturn(seats);

        ResponseEntity<List<SeatDTO>> response = eventController.getBestSeats(VALID_EVENT_ID, 5);

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
    private List<EventDTO> getEvents() {
        return List.of(
                EventDTO.builder().eventId("1").eventDate(LocalDate.parse("2022-01-01")).name("Event 001").build(),
                EventDTO.builder().eventId("2").eventDate(LocalDate.parse("2022-01-02")).name("Event 002").build(),
                EventDTO.builder().eventId("3").eventDate(LocalDate.parse("2022-01-03")).name("Event 003").build()
        );
    }

    private List<SeatDTO> getSeats() {
        return List.of(
                SeatDTO.builder().seatNumber("2").row("17").level("b").section("E").status(SeatStatus.OPEN).build(),
                SeatDTO.builder().seatNumber("3").row("35").level("t").section("K").status(SeatStatus.OPEN).build(),
                SeatDTO.builder().seatNumber("3").row("30").level("z").section("f").status(SeatStatus.OPEN).build()

        );
    }

    private SeatRequest getSeatRequest() {
        SeatRequest seatRequest = new SeatRequest();
        seatRequest.setSeatNumber("2");
        seatRequest.setRow("17");
        seatRequest.setLevel("b");
        seatRequest.setSection("E");
        return seatRequest;
    }
    /* stubs - END */
}
