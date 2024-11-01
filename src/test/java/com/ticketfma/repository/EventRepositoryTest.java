package com.ticketfma.repository;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ticketfma.domain.Event;
import com.ticketfma.domain.Seat;
import com.ticketfma.domain.enums.SeatStatus;
import com.ticketfma.dto.SeatRequest;
import com.ticketfma.exception.SeatUnavailableException;

@ExtendWith(MockitoExtension.class)
public class EventRepositoryTest {

    @InjectMocks
    private EventRepository eventRepository;

    @Mock
    private CsvDataLoader csvDataLoader;

    @BeforeEach
    public void setUp() {
        doNothing().when(csvDataLoader).loadCsvData();
        when(csvDataLoader.getEvents()).thenReturn(getEvents());
        when(csvDataLoader.getEventSeats()).thenReturn(getEventSeats());

        eventRepository.loadCsvData();
    }

    @Test
    public void testConcurrentSeatReservations() throws InterruptedException {
        String eventId = "event1";
        SeatRequest seatRequest = getSeatRequest();

        // Prepare tasks to reserve the same seat in multiple threads.
        Callable<Void> reserveTask = () -> {
            try {
                eventRepository.reserveSeats(eventId, List.of(seatRequest));
            } catch (SeatUnavailableException e) {
                // Expected when seat is already reserved.
            }
            return null;
        };

        // Run the tasks concurrently.
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Callable<Void>> tasks = Collections.nCopies(10, reserveTask);

        List<Future<Void>> results = executorService.invokeAll(tasks);

        // Wait for all tasks to complete.
        for (Future<Void> result : results) {
            try {
                result.get(); // This will rethrow any exception thrown in the threads.
            } catch (ExecutionException e) {
                Assertions.assertInstanceOf(SeatUnavailableException.class, e.getCause(), "Expected SeatUnavailableException when seat is already reserved");
            }
        }

        // Validate that seat was reserved by only one thread.
        Optional<Seat> reservedSeat = eventRepository.getSeat("event1", "9", "AA", "1", "Ground");
        Assertions.assertTrue(reservedSeat.isPresent(), "Seat should exist");
        Assertions.assertEquals(SeatStatus.HOLD, reservedSeat.get().getStatus(), "Seat should be on hold");

        // Ensure the locks were released and no locks remain.
        Assertions.assertFalse(eventRepository.getEventLocks().containsKey(eventId), "Lock should be removed after reservation attempts complete");
        Assertions.assertFalse(eventRepository.getLockCounts().containsKey(eventId), "Lock count should be removed after reservation attempts complete");

        executorService.shutdown();
    }

    /* stubs - BEGIN */
    private SeatRequest getSeatRequest() {
        SeatRequest seatRequest = new SeatRequest();
        seatRequest.setLevel("1");
        seatRequest.setSection("Ground");
        seatRequest.setRow("AA");
        seatRequest.setSeatNumber("9");
        return seatRequest;
    }

    private List<Event> getEvents() {
        return Arrays.asList(
                Event.builder().eventId("event1").name("Event 1").build(),
                Event.builder().eventId("event2").name("Event 2").build(),
                Event.builder().eventId("event3").name("Event 3").build()
        );
    }

    private ConcurrentHashMap<String, List<Seat>> getEventSeats() {
        ConcurrentHashMap<String, List<Seat>> eventSeats = new ConcurrentHashMap<>();
        eventSeats.put("event1", Collections.singletonList(
                Seat.builder().level("1").section("Ground").row("AA").seatNumber("9").status(SeatStatus.OPEN).build())
        );
        eventSeats.put("event2", Collections.singletonList(
                Seat.builder().level("2").section("East").row("BB").seatNumber("8").status(SeatStatus.OPEN).build())
        );
        eventSeats.put("event3", Collections.singletonList(
                Seat.builder().level("3").section("West").row("CC").seatNumber("7").status(SeatStatus.OPEN).build())
        );
        return eventSeats;
    }
    /* stubs - END */
}
