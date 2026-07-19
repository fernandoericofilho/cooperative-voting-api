package com.sicredi.voting.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sicredi.voting.exceptions.AgendaNotFoundException;
import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.services.AgendaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AgendaController.class)
class AgendaControllerCoverageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgendaService agendaService;

    @MockBean
    private com.sicredi.voting.mappers.AgendaMapper agendaMapper;

    @MockBean
    private com.sicredi.voting.mappers.VotingResultMapper votingResultMapper;

    @Test
    void getAgendaByIdReturns200() throws Exception {
        Agenda agenda = new Agenda("Reform", "Description");
        agenda.setId(1L);

        when(agendaService.findById(1L)).thenReturn(agenda);
        when(agendaMapper.toAgendaDTO(any())).thenReturn(
            new com.sicredi.voting.controllers.response.AgendaResponse(
                1L, "Reform", "Description", "2026-01-01", null, null, "NOT_STARTED"
            )
        );

        mockMvc.perform(get("/api/v1/agendas/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.title", is("Reform")))
            .andExpect(jsonPath("$.description", is("Description")));
    }

    @Test
    void getAgendaByIdReturns404() throws Exception {
        when(agendaService.findById(999L)).thenThrow(new AgendaNotFoundException(999L));

        mockMvc.perform(get("/api/v1/agendas/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getAgendaByIdWithOpenSession() throws Exception {
        Agenda agenda = new Agenda("Voting", "Description");
        agenda.setId(2L);
        agenda.openSession(60L);

        when(agendaService.findById(2L)).thenReturn(agenda);
        when(agendaMapper.toAgendaDTO(any())).thenReturn(
            new com.sicredi.voting.controllers.response.AgendaResponse(
                2L, "Voting", "Description", "2026-01-01", "2026-01-01T10:00:00", "2026-01-01T10:01:00", "OPEN"
            )
        );

        mockMvc.perform(get("/api/v1/agendas/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title", is("Voting")))
            .andExpect(jsonPath("$.status", is("OPEN")));
    }

    @Test
    void getAgendaByIdVerifyMapperCalled() throws Exception {
        Agenda agenda = new Agenda("Test", "Desc");
        agenda.setId(5L);

        when(agendaService.findById(5L)).thenReturn(agenda);
        when(agendaMapper.toAgendaDTO(any())).thenReturn(
            new com.sicredi.voting.controllers.response.AgendaResponse(
                5L, "Test", "Desc", "2026-01-01", null, null, "NOT_STARTED"
            )
        );

        mockMvc.perform(get("/api/v1/agendas/5"))
            .andExpect(status().isOk());

        verify(agendaService).findById(5L);
        verify(agendaMapper).toAgendaDTO(any());
    }

    @Test
    void getAgendaByIdResponseContent() throws Exception {
        Agenda agenda = new Agenda("New Law", "Law description");
        agenda.setId(10L);

        when(agendaService.findById(10L)).thenReturn(agenda);
        when(agendaMapper.toAgendaDTO(any())).thenReturn(
            new com.sicredi.voting.controllers.response.AgendaResponse(
                10L, "New Law", "Law description", "2026-01-01", null, null, "NOT_STARTED"
            )
        );

        mockMvc.perform(get("/api/v1/agendas/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.title").value("New Law"))
            .andExpect(jsonPath("$.createdAt").value("2026-01-01"));
    }
}
