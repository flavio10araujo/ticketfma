package com.ticketfma.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ticketfma.domain.Seat;
import com.ticketfma.service.EventService;

@ExtendWith(MockitoExtension.class)
public class EventControllerTest {

    private static final String VALID_EVENT_ID = "101";
    private static final String INVALID_EVENT_ID = "999";

    @InjectMocks
    private EventController eventController;

    @Mock
    private EventService eventService;

    /* getEvents - BEGIN */
    @Test
    public void givenNoSort_whenGetEvents_thenReturnAllEvents() {
        eventController.getEvents(Optional.empty());
        verify(eventService).getAllEvents(Optional.empty());
    }

    @Test
    public void givenSortByName_whenGetEvents_thenReturnAllEventsSortedByName() {
        eventController.getEvents(Optional.of("name"));
        verify(eventService).getAllEvents(Optional.of("name"));
    }

    @Test
    public void givenSortByDate_whenGetEvents_thenReturnAllEventsSortedByDate() {
        eventController.getEvents(Optional.of("date"));
        verify(eventService).getAllEvents(Optional.of("date"));
    }
    /* getEvents - END */

    /* getBestSeats - BEGIN */
    @Test
    public void givenValidEventIdAndQuantity_whenGetBestSeats_thenReturnBestSeats() {
        eventController.getBestSeats(VALID_EVENT_ID, 5);
        verify(eventService).getBestSeats(VALID_EVENT_ID, 5);
    }

    @Test
    public void givenInvalidEventId_whenGetBestSeats_thenReturnNotFound() {
        when(eventService.getBestSeats(INVALID_EVENT_ID, 5)).thenThrow(new NoSuchElementException());

        ResponseEntity<List<Seat>> response = eventController.getBestSeats(INVALID_EVENT_ID, 5);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    /* getBestSeats - END */
}
