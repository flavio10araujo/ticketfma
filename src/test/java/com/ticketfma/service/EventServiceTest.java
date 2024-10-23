package com.ticketfma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ticketfma.repository.IEventRepository;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    private static final String VALID_EVENT_ID = "101";
    private static final String INVALID_EVENT_ID = "999";

    @InjectMocks
    private EventService eventService;

    @Mock
    private IEventRepository repository;

    /* getAllEvents - BEGIN */
    @Test
    public void whenGetAllEvents_thenReturnAllEvents() {
        eventService.getAllEvents(Optional.empty());
        verify(repository).getAllEvents(Optional.empty());
    }
    /* getAllEvents - END */

    /* getBestSeats - BEGIN */
    @Test
    public void givenInvalidEventId_whenGetBestSeats_thenThrowNoSuchElementException() {
        when(repository.eventExists(INVALID_EVENT_ID)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            eventService.getBestSeats(INVALID_EVENT_ID, 5);
        });

        assertEquals("Event not found.", exception.getMessage());
    }

    @Test
    public void givenValidEventId_whenGetBestSeats_thenReturnBestSeats() {
        when(repository.eventExists(VALID_EVENT_ID)).thenReturn(true);

        eventService.getBestSeats(VALID_EVENT_ID, 5);
        verify(repository).getBestSeats(VALID_EVENT_ID, 5);
    }
    /* getBestSeats - END */
}
