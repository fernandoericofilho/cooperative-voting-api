package com.sicredi.voting.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.voting.controllers.request.CreateAgendaRequest;
import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.services.AgendaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

@WebMvcTest(AgendaController.class)
class AgendaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgendaService agendaService;

    @MockBean
    private com.sicredi.voting.mappers.AgendaMapper agendaMapper;

    @MockBean
    private com.sicredi.voting.mappers.VotingResultMapper votingResultMapper;

    @Test
    void createAgendaReturns201() throws Exception {
        Agenda agenda = new Agenda("Reform", "Description");
        agenda.setId(1L);

        when(agendaService.createAgenda("Reform", "Description")).thenReturn(agenda);
        when(agendaMapper.toAgendaDTO(any())).thenReturn(
            new com.sicredi.voting.controllers.response.AgendaResponse(
                1L, "Reform", "Description", "2026-01-01", null, null, "NOT_STARTED"
            )
        );

        CreateAgendaRequest request = new CreateAgendaRequest("Reform", "Description");

        mockMvc.perform(post("/api/v1/agendas")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Reform")));
    }

    @Test
    void listAgendasReturns200() throws Exception {
        Agenda agenda1 = new Agenda("Reform", "Desc1");
        Agenda agenda2 = new Agenda("Law", "Desc2");

        var page = new PageImpl<>(List.of(agenda1, agenda2), PageRequest.of(0, 10), 2);
        when(agendaService.listAgendas(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/agendas?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void listAgendasEmpty() throws Exception {
        var emptyPage = new PageImpl<Agenda>(List.of(), PageRequest.of(0, 10), 0);
        when(agendaService.listAgendas(any())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/agendas?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void openSessionReturns200() throws Exception {
        Agenda agenda = new Agenda("Reform", "Desc");
        agenda.openSession(60L);

        when(agendaService.openSession(1L, 60L)).thenReturn(agenda);

        mockMvc.perform(post("/api/v1/agendas/1/sessions")
                .contentType("application/json")
                .content("{\"durationSeconds\": 60}"))
                .andExpect(status().isOk());
    }

    @Test
    void tallyResultReturns200() throws Exception {
        com.sicredi.voting.dtos.AgendaResultDto result =
            com.sicredi.voting.dtos.AgendaResultDto.calculate(5, 3);

        when(agendaService.tallyResult(1L)).thenReturn(result);
        when(votingResultMapper.toResultDTO(any(), any())).thenReturn(
            new com.sicredi.voting.controllers.response.VotingResultResponse(
                1L, "Reform", 5L, 3L, "APPROVED"
            )
        );

        mockMvc.perform(get("/api/v1/agendas/1/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yesCount", is(5)))
                .andExpect(jsonPath("$.noCount", is(3)));
    }
}
