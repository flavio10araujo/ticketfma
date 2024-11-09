package com.ticketfma.service.impl;

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

import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.domain.enums.SeatStatus;
import com.ticketfma.dto.EventDTO;
import com.ticketfma.dto.SeatDTO;
import com.ticketfma.dto.SeatRequest;
import com.ticketfma.exception.EventNotFoundException;
import com.ticketfma.repository.impl.EventRepository;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    private static final String VALID_EVENT_ID = "101";
    private static final String INVALID_EVENT_ID = "999";
    private static final String SORT_BY_NAME = "name";
    private static final String SORT_BY_DATE = "date";

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository repository;

    /* getAllEvents - BEGIN */
    @Test
    public void givenNoSort_whenGetAllEvents_thenReturnAllEvents() {
        List<Event> events = getEvents();
        when(repository.getAllEvents(null)).thenReturn(events);

        List<EventDTO> allEvents = eventService.getAllEvents(null);

        assertEquals(events.size(), allEvents.size());
        for (int i = 0; i < events.size(); i++) {
            isSameEvent(events.get(i), allEvents.get(i));
        }
        verify(repository).getAllEvents(null);
    }

    @Test
    public void givenSortByName_whenGetAllEvents_thenReturnAllEventsSortedByName() {
        List<Event> events = getEvents();
        when(repository.getAllEvents(SORT_BY_NAME)).thenReturn(events);

        List<EventDTO> eventsSortedByName = eventService.getAllEvents(SORT_BY_NAME);

        assertEquals(events.size(), eventsSortedByName.size());
        for (int i = 0; i < events.size(); i++) {
            isSameEvent(events.get(i), eventsSortedByName.get(i));
        }
        verify(repository).getAllEvents(SORT_BY_NAME);
    }

    @Test
    public void givenSortByDate_whenGetAllEvents_thenReturnAllEventsSortedByDate() {
        List<Event> events = getEvents();
        when(repository.getAllEvents(SORT_BY_DATE)).thenReturn(events);

        List<EventDTO> eventsSortedByDate = eventService.getAllEvents(SORT_BY_DATE);

        assertEquals(events.size(), eventsSortedByDate.size());
        for (int i = 0; i < events.size(); i++) {
            isSameEvent(events.get(i), eventsSortedByDate.get(i));
        }
        verify(repository).getAllEvents(SORT_BY_DATE);
    }
    /* getAllEvents - END */

    /* getSeat - BEGIN */
    @Test
    public void givenInvalidEventId_whenGetSeat_thenThrowNoSuchElementException() {
        when(repository.eventExists(INVALID_EVENT_ID)).thenReturn(false);

        EventNotFoundException exception = assertThrows(EventNotFoundException.class, () -> {
            eventService.getSeat(INVALID_EVENT_ID, new SeatRequest());
        });

        assertEquals("Event '" + INVALID_EVENT_ID + "' not found.", exception.getMessage());
    }

    @Test
    public void givenValidEventIdAndValidSeatRequest_whenGetSeat_thenReturnSeat() {
        SeatRequest seatRequest = new SeatRequest();
        Seat seat = getSeats().getFirst();
        when(repository.eventExists(VALID_EVENT_ID)).thenReturn(true);
        when(repository.getSeat(VALID_EVENT_ID, seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(),
                seatRequest.getSection())).thenReturn(Optional.ofNullable(seat));

        SeatDTO seatReturned = eventService.getSeat(VALID_EVENT_ID, seatRequest).get();

        isSameSeat(seat, seatReturned);
        verify(repository).getSeat(VALID_EVENT_ID, seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection());
    }

    @Test
    public void givenValidEventIdAndInvalidSeatRequest_whenGetSeat_thenReturnEmpty() {
        SeatRequest seatRequest = new SeatRequest();
        when(repository.eventExists(VALID_EVENT_ID)).thenReturn(true);
        when(repository.getSeat(VALID_EVENT_ID, seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(),
                seatRequest.getSection())).thenReturn(Optional.empty());

        Optional<SeatDTO> seatReturned = eventService.getSeat(VALID_EVENT_ID, seatRequest);

        assertEquals(Optional.empty(), seatReturned);
        verify(repository).getSeat(VALID_EVENT_ID, seatRequest.getSeatNumber(), seatRequest.getRow(), seatRequest.getLevel(), seatRequest.getSection());
    }
    /* getSeat - END */

    /* getBestSeats - BEGIN */
    @Test
    public void givenInvalidEventId_whenGetBestSeats_thenThrowNoSuchElementException() {
        when(repository.eventExists(INVALID_EVENT_ID)).thenReturn(false);

        EventNotFoundException exception = assertThrows(EventNotFoundException.class, () -> {
            eventService.getBestSeats(INVALID_EVENT_ID, 5);
        });

        assertEquals("Event '" + INVALID_EVENT_ID + "' not found.", exception.getMessage());
    }

    @Test
    public void givenValidEventId_whenGetBestSeats_thenReturnBestSeats() {
        List<Seat> seats = getSeats();
        when(repository.eventExists(VALID_EVENT_ID)).thenReturn(true);
        when(repository.getBestSeats(VALID_EVENT_ID, 5)).thenReturn(seats);

        List<SeatDTO> bestSeats = eventService.getBestSeats(VALID_EVENT_ID, 5);

        assertEquals(seats.size(), bestSeats.size());
        for (int i = 0; i < seats.size(); i++) {
            isSameSeat(seats.get(i), bestSeats.get(i));
        }
        verify(repository).getBestSeats(VALID_EVENT_ID, 5);
    }
    /* getBestSeats - END */

    /* reserveSeats - BEGIN */
    @Test
    public void givenValidEventIdAndValidSeatRequest_whenReserveSeats_thenReserveSeatRequest() {
        List<SeatRequest> seatRequests = List.of(new SeatRequest());
        when(repository.eventExists(VALID_EVENT_ID)).thenReturn(true);
        when(repository.seatExists(VALID_EVENT_ID, seatRequests.getFirst().getSeatNumber(), seatRequests.getFirst().getRow(),
                seatRequests.getFirst().getLevel(),
                seatRequests.getFirst().getSection())).thenReturn(true);
        when(repository.seatAvailable(VALID_EVENT_ID, seatRequests.getFirst().getSeatNumber(), seatRequests.getFirst().getRow(),
                seatRequests.getFirst().getLevel(),
                seatRequests.getFirst().getSection())).thenReturn(true);

        eventService.reserveSeats(VALID_EVENT_ID, seatRequests);

        verify(repository).eventExists(VALID_EVENT_ID);
        verify(repository).seatExists(VALID_EVENT_ID, seatRequests.getFirst().getSeatNumber(), seatRequests.getFirst().getRow(),
                seatRequests.getFirst().getLevel(), seatRequests.getFirst().getSection());
        verify(repository).seatAvailable(VALID_EVENT_ID, seatRequests.getFirst().getSeatNumber(), seatRequests.getFirst().getRow(),
                seatRequests.getFirst().getLevel(), seatRequests.getFirst().getSection());
        verify(repository).reserveSeats(VALID_EVENT_ID, seatRequests);
    }
    /* reserveSeats - END */

    private void isSameEvent(Event event, EventDTO eventDTO) {
        assertEquals(event.getEventId(), eventDTO.getEventId());
        assertEquals(event.getName(), eventDTO.getName());
        assertEquals(event.getEventDate(), eventDTO.getEventDate());
    }

    private void isSameSeat(Seat seat, SeatDTO seatDTO) {
        assertEquals(seat.getSeatNumber(), seatDTO.getSeatNumber());
        assertEquals(seat.getRow(), seatDTO.getRow());
        assertEquals(seat.getLevel(), seatDTO.getLevel());
        assertEquals(seat.getSection(), seatDTO.getSection());
        assertEquals(seat.getStatus(), seatDTO.getStatus());
    }

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
