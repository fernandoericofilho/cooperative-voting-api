package com.sicredi.voting.dtos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.voting.controllers.response.AgendaResponse;
import com.sicredi.voting.controllers.response.VotingResultResponse;
import com.sicredi.voting.controllers.response.VoteResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class DomainDTOSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void agendaDTOSerializesCorrectly() throws Exception {
        String now = LocalDateTime.now().format(FORMATTER);
        AgendaResponse agenda = new AgendaResponse(1L, "Title", "Description", now, null, null, "NOT_STARTED");

        String json = mapper.writeValueAsString(agenda);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"title\":\"Title\""));
        assertFalse(json.contains("sessionOpenedAt")); // null fields excluded
    }

    @Test
    void voteDTOSerializesCorrectly() throws Exception {
        String now = LocalDateTime.now().format(FORMATTER);
        VoteResponse vote = new VoteResponse(1L, 1L, "12345678901", "YES", now);

        String json = mapper.writeValueAsString(vote);
        assertTrue(json.contains("\"vote\":\"YES\""));
        assertTrue(json.contains("\"memberCpf\":\"12345678901\""));
    }

    @Test
    void resultDTOSerializesCorrectly() throws Exception {
        VotingResultResponse result = new VotingResultResponse(1L, "Title", 10L, 5L, "YES");

        String json = mapper.writeValueAsString(result);
        assertTrue(json.contains("\"yesCount\":10"));
        assertTrue(json.contains("\"result\":\"YES\""));
    }
}
