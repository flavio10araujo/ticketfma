package com.ticketfma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.domain.enums.SeatStatus;
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
    public void givenNoSort_whenGetAllEvents_thenReturnAllEvents() {
        List<Event> events = getEvents();
        when(repository.getAllEvents(Optional.empty())).thenReturn(events);

        List<Event> allEvents = eventService.getAllEvents(Optional.empty());

        assertEquals(events, allEvents);
        verify(repository).getAllEvents(Optional.empty());
    }

    @Test
    public void givenSortByName_whenGetAllEvents_thenReturnAllEventsSortedByName() {
        List<Event> events = getEvents();
        when(repository.getAllEvents(Optional.of("name"))).thenReturn(events);

        List<Event> eventsSortedByName = eventService.getAllEvents(Optional.of("name"));

        assertEquals(events, eventsSortedByName);
        verify(repository).getAllEvents(Optional.of("name"));
    }

    @Test
    public void givenSortByDate_whenGetAllEvents_thenReturnAllEventsSortedByDate() {
        List<Event> events = getEvents();
        when(repository.getAllEvents(Optional.of("date"))).thenReturn(events);

        List<Event> eventsSortedByDate = eventService.getAllEvents(Optional.of("date"));

        assertEquals(events, eventsSortedByDate);
        verify(repository).getAllEvents(Optional.of("date"));
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
        List<Seat> seats = getSeats();
        when(repository.eventExists(VALID_EVENT_ID)).thenReturn(true);
        when(repository.getBestSeats(VALID_EVENT_ID, 5)).thenReturn(seats);

        List<Seat> bestSeats = eventService.getBestSeats(VALID_EVENT_ID, 5);

        assertEquals(seats, bestSeats);
        verify(repository).getBestSeats(VALID_EVENT_ID, 5);
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
