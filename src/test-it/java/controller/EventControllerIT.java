package controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ticketfma.Application;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class EventControllerIT {

    private static final String URI_GET_EVENTS = "/api/v1/events";
    private static final String URI_GET_BEST_SEATS_SUFFIX = "/best-seats";
    private static final String VALID_EVENT_ID = "3001";
    private static final String INVALID_EVENT_ID = "9999";
    private static final String PARAM_SORT = "sort";
    private static final String PARAM_QUANTITY = "quantity";

    @Autowired
    private MockMvc mockMvc;

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

    /* /api/v1/events/{eventId}/best-seats - BEGIN */
    @Test
    public void givenValidEventIdAndQuantity_whenGetBestSeats_thenReturnBestSeats() throws Exception {
        String responseContent = mockMvc.perform(get(URI_GET_EVENTS + "/" + VALID_EVENT_ID + URI_GET_BEST_SEATS_SUFFIX)
                        .param(PARAM_QUANTITY, "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // We have 2 seats OPEN for the event "3001" in the data.csv.
                .andExpect(jsonPath("$[0].seatNumber").value("32")) // The best seat for the event "3001" is "32" in the data.csv.
                .andReturn().getResponse().getContentAsString();

        assertThat(responseContent).contains(
                "{\"seatNumber\":\"32\",\"row\":\"C1\",\"level\":\"C\",\"section\":\"S3\",\"status\":\"OPEN\",\"sellRank\":2,\"hasUpsells\":false}",
                "{\"seatNumber\":\"31\",\"row\":\"C1\",\"level\":\"C\",\"section\":\"S3\",\"status\":\"OPEN\",\"sellRank\":4,\"hasUpsells\":false}"
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
    public void givenInvalidQuantity_whenGetBestSeats_thenReturnError400(int invalidQuantity) throws Exception {
        mockMvc.perform(get(URI_GET_EVENTS + "/" + VALID_EVENT_ID + URI_GET_BEST_SEATS_SUFFIX)
                        .param(PARAM_QUANTITY, String.valueOf(invalidQuantity))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    /* /api/v1/events/{eventId}/best-seats - END */
}