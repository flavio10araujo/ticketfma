package controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketfma.Application;
import com.ticketfma.dto.SeatRequest;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class EventControllerIT {

    private static final String URI_GET_EVENTS = "/api/v1/events";
    private static final String URI_GET_BEST_SEATS_SUFFIX = "/best-seats";
    private static final String URI_SEARCH_SEAT_SUFFIX = "/search-seat";
    private static final String URI_RESERVE_SEATS_SUFFIX = "/reserve-seats";
    private static final String VALID_EVENT_ID = "3001";
    private static final String INVALID_EVENT_ID = "9999";
    private static final String PARAM_SORT = "sort";
    private static final String PARAM_QUANTITY = "quantity";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /* /api/v1/events - BEGIN */
    @Test
    public void givenNoSort_whenGetEvents_thenReturnAllEvents() throws Exception {
        mockMvc.perform(get(URI_GET_EVENTS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4)); // We have 4 events in the data.csv.
    }

    @Test
    public void givenSortByName_whenGetEvents_thenReturnAllEventsSortedByName() throws Exception {
        mockMvc.perform(get(URI_GET_EVENTS)
                        .param(PARAM_SORT, "name")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].eventId").value("1000")) // When sorted by name, the first event is "1000" in the data.csv.
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void givenSortByDate_whenGetEvents_thenReturnAllEventsSortedByDate() throws Exception {
        mockMvc.perform(get(URI_GET_EVENTS)
                        .param(PARAM_SORT, "date")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].eventId").value("3001")) // When sorted by date, the first event is "3001" in the data.csv.
                .andReturn().getResponse().getContentAsString();
    }
    /* /api/v1/events - END */

    /* /api/v1/events/{eventId}/search-seat - BEGIN */
    @Test
    public void givenValidEventIdAndValidSeatRequest_whenGetSeat_thenReturnSeat() throws Exception {
        String responseContent = mockMvc.perform(post(URI_GET_EVENTS + "/" + VALID_EVENT_ID + URI_SEARCH_SEAT_SUFFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getValidButNotAvailableSeatRequest())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(responseContent).contains(
                "{\"seatNumber\":\"33\",\"row\":\"C1\",\"level\":\"C\",\"section\":\"S3\",\"status\":\"HOLD\"}"
        );
    }

    @Test
    public void givenInvalidEventId_whenGetSeat_thenReturnNotFound() throws Exception {
        mockMvc.perform(post(URI_GET_EVENTS + "/" + INVALID_EVENT_ID + URI_SEARCH_SEAT_SUFFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getValidButNotAvailableSeatRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenValidEventIdAndInvalidSeatRequest_whenGetSeat_thenReturnNotFound() throws Exception {
        mockMvc.perform(post(URI_GET_EVENTS + "/" + VALID_EVENT_ID + URI_SEARCH_SEAT_SUFFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getInvalidSeatRequest())))
                .andExpect(status().isNotFound());
    }
    /* /api/v1/events/{eventId}/search-seat - END */

    /* /api/v1/events/{eventId}/best-seats - BEGIN */
    @Test
    public void givenValidEventIdAndValidQuantity_whenGetBestSeats_thenReturnBestSeats() throws Exception {
        String responseContent = mockMvc.perform(get(URI_GET_EVENTS + "/" + VALID_EVENT_ID + URI_GET_BEST_SEATS_SUFFIX)
                        .param(PARAM_QUANTITY, "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // We have 2 seats OPEN for the event "3001" in the data.csv.
                .andExpect(jsonPath("$[0].seatNumber").value("32")) // The best seat for the event "3001" is "32" in the data.csv.
                .andReturn().getResponse().getContentAsString();

        assertThat(responseContent).contains(
                "{\"seatNumber\":\"32\",\"row\":\"C1\",\"level\":\"C\",\"section\":\"S3\",\"status\":\"OPEN\"}",
                "{\"seatNumber\":\"31\",\"row\":\"C1\",\"level\":\"C\",\"section\":\"S3\",\"status\":\"OPEN\"}"
        );
    }

    @Test
    public void givenInvalidEventId_whenGetBestSeats_thenReturnNotFound() throws Exception {
        mockMvc.perform(get(URI_GET_EVENTS + "/" + INVALID_EVENT_ID + URI_GET_BEST_SEATS_SUFFIX)
                        .param(PARAM_QUANTITY, "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, Integer.MIN_VALUE })
    public void givenValidEventIdAndInvalidQuantity_whenGetBestSeats_thenReturnError400(int invalidQuantity) throws Exception {
        mockMvc.perform(get(URI_GET_EVENTS + "/" + VALID_EVENT_ID + URI_GET_BEST_SEATS_SUFFIX)
                        .param(PARAM_QUANTITY, String.valueOf(invalidQuantity))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    /* /api/v1/events/{eventId}/best-seats - END */

    /* /v1/events/{eventId}/reserve-seats - BEGIN */
    @Test
    public void givenInvalidEventId_whenReserveSeats_thenReturnNotFound() throws Exception {
        mockMvc.perform(post(URI_GET_EVENTS + "/" + INVALID_EVENT_ID + URI_RESERVE_SEATS_SUFFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(getValidButNotAvailableSeatRequest()))))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenValidEventIdAndInvalidSeatRequest_whenReserveSeats_thenReserveSeats() throws Exception {
        mockMvc.perform(post(URI_GET_EVENTS + "/" + VALID_EVENT_ID + URI_RESERVE_SEATS_SUFFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(getInvalidSeatRequest()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenValidEventIdAndSeatRequestNotAvailable_whenReserveSeats_thenReserveSeats() throws Exception {
        mockMvc.perform(post(URI_GET_EVENTS + "/" + VALID_EVENT_ID + URI_RESERVE_SEATS_SUFFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(getValidButNotAvailableSeatRequest()))))
                .andExpect(status().isConflict());
    }

    @Test
    public void givenValidEventIdAndSeatRequestAvailable_whenReserveSeats_thenReserveSeats() throws Exception {
        String responseContentBefore = mockMvc.perform(post(URI_GET_EVENTS + "/4001" + URI_SEARCH_SEAT_SUFFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getValidAndAvailableSeatRequest())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Checking if the seat is available.
        assertThat(responseContentBefore).contains(
                "{\"seatNumber\":\"40\",\"row\":\"D1\",\"level\":\"D\",\"section\":\"S4\",\"status\":\"OPEN\"}"
        );

        mockMvc.perform(post(URI_GET_EVENTS + "/4001" + URI_RESERVE_SEATS_SUFFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(getValidAndAvailableSeatRequest()))))
                .andExpect(status().isCreated());

        String responseContentAfter = mockMvc.perform(post(URI_GET_EVENTS + "/4001" + URI_SEARCH_SEAT_SUFFIX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getValidAndAvailableSeatRequest())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Checking if the seat is now on HOLD.
        assertThat(responseContentAfter).contains(
                "{\"seatNumber\":\"40\",\"row\":\"D1\",\"level\":\"D\",\"section\":\"S4\",\"status\":\"HOLD\"}"
        );
    }
    /* /v1/events/{eventId}/reserve-seats - END */

    /**
     * This method returns a valid seat from data.csv related to eventId = 3001.
     * Although the seat is valid, its status is HOLD.
     *
     * @return a valid seat from data.csv with status HOLD.
     */
    private SeatRequest getValidButNotAvailableSeatRequest() {
        SeatRequest seatRequest = new SeatRequest();
        seatRequest.setSeatNumber("33");
        seatRequest.setRow("C1");
        seatRequest.setLevel("C");
        seatRequest.setSection("S3");
        return seatRequest;
    }

    /**
     * This method returns a valid and available seat from data.csv related to eventId = 4001.
     *
     * @return a valid seat from data.csv with status OPEN.
     */
    private SeatRequest getValidAndAvailableSeatRequest() {
        SeatRequest seatRequest = new SeatRequest();
        seatRequest.setSeatNumber("40");
        seatRequest.setRow("D1");
        seatRequest.setLevel("D");
        seatRequest.setSection("S4");
        return seatRequest;
    }

    private SeatRequest getInvalidSeatRequest() {
        SeatRequest seatRequest = new SeatRequest();
        seatRequest.setSeatNumber("99");
        seatRequest.setRow("C1");
        seatRequest.setLevel("C");
        seatRequest.setSection("S3");
        return seatRequest;
    }
}
