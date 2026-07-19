package com.sicredi.voting.exceptions;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sicredi.voting.controllers.AgendaController;
import com.sicredi.voting.services.AgendaService;
import com.sicredi.voting.services.VoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AgendaController.class)
class GlobalExceptionHandlerExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgendaService agendaService;

    @MockBean
    private com.sicredi.voting.mappers.AgendaMapper agendaMapper;

    @MockBean
    private com.sicredi.voting.mappers.VotingResultMapper votingResultMapper;

    // AgendaNotFoundException -> 404 NOT_FOUND
    @Test
    void handleAgendaNotFoundReturns404() throws Exception {
        when(agendaService.findById(999L))
            .thenThrow(new AgendaNotFoundException(999L));

        mockMvc.perform(get("/api/v1/agendas/999/result"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Agenda")));
    }

    // SessionAlreadyOpenException -> 409 CONFLICT
    @Test
    void handleSessionAlreadyOpenReturns409() throws Exception {
        when(agendaService.openSession(1L, 60L))
            .thenThrow(new SessionAlreadyOpenException(1L));

        mockMvc.perform(post("/api/v1/agendas/1/sessions")
                .contentType("application/json")
                .content("{\"durationSeconds\": 60}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Session")));
    }

    // Note: SessionNotOpenException and SessionClosedException are tested via VoteController, not AgendaController
    // They are raised when registering a vote, not when opening a session

    // MethodArgumentNotValidException -> 400 BAD_REQUEST with validation
    @Test
    void handleValidationErrorReturns400WithDetails() throws Exception {
        mockMvc.perform(post("/api/v1/agendas")
                .contentType("application/json")
                .content("{\"title\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Validation")));
    }

    // General Exception -> 500 INTERNAL_SERVER_ERROR
    @Test
    void handleGeneralExceptionReturns500() throws Exception {
        when(agendaService.findById(1L))
            .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/v1/agendas/1/result"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("Internal")));
    }

    // DuplicateVoteException -> 409 CONFLICT
    @Test
    void handleDuplicateVoteReturns409() throws Exception {
        when(agendaService.tallyResult(1L))
            .thenThrow(new DuplicateVoteException(1L, "12345678901"));

        mockMvc.perform(get("/api/v1/agendas/1/result"))
                .andExpect(status().isConflict());
    }

    // MemberNotEligibleException -> 422 UNPROCESSABLE_ENTITY
    @Test
    void handleMemberNotEligibleReturns422() throws Exception {
        when(agendaService.tallyResult(1L))
            .thenThrow(new MemberNotEligibleException("12345678901"));

        mockMvc.perform(get("/api/v1/agendas/1/result"))
                .andExpect(status().is(422));
    }
}
