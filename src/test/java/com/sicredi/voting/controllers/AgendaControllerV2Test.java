package com.sicredi.voting.controllers;

import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.services.AgendaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgendaControllerV2.class)
class AgendaControllerV2Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgendaService agendaService;

    @Test
    void listAgendas_shouldReturnSimplifiedFormat() throws Exception {
        Agenda agenda1 = new Agenda("Agenda 1", "Description 1");
        agenda1.setId(1L);

        Agenda agenda2 = new Agenda("Agenda 2", "Description 2");
        agenda2.setId(2L);

        when(agendaService.listAll()).thenReturn(List.of(agenda1, agenda2));

        mockMvc.perform(get("/api/v2/agendas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].title", is("Agenda 1")))
            .andExpect(jsonPath("$[0].status", notNullValue()))
            .andExpect(jsonPath("$[0].createdAt", notNullValue()))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].title", is("Agenda 2")));
    }

    @Test
    void listAgendas_emptyList() throws Exception {
        when(agendaService.listAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v2/agendas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }
}
