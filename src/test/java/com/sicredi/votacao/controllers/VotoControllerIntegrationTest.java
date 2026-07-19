package com.sicredi.votacao.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.votacao.controllers.request.RegistrarVotoRequest;
import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.models.Voto;
import com.sicredi.votacao.services.VotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VotoController.class)
class VotoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VotoService votoService;

    @MockBean
    private com.sicredi.votacao.mappers.VotoMapper votoMapper;

    @Test
    void registrarVotoReturns201() throws Exception {
        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);
        voto.setId(1L);

        when(votoService.registrarVoto(1L, "12345678901", OpcaoVoto.SIM)).thenReturn(voto);
        when(votoMapper.toVotoDTO(any())).thenReturn(
            new com.sicredi.votacao.controllers.response.VotoResponse(
                1L, 1L, "123.***.**-01", "SIM", "2026-01-01"
            )
        );

        RegistrarVotoRequest request = new RegistrarVotoRequest("12345678901", "SIM");

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voto", is("SIM")))
                .andExpect(jsonPath("$.pautaId", is(1)));
    }

    @Test
    void registrarVotoComCpfInvalidoRetorna400() throws Exception {
        RegistrarVotoRequest request = new RegistrarVotoRequest("123", "SIM");

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarVotoNaoRetorna201() throws Exception {
        Voto voto = new Voto(1L, "98765432109", OpcaoVoto.NAO);
        voto.setId(2L);

        when(votoService.registrarVoto(1L, "98765432109", OpcaoVoto.NAO)).thenReturn(voto);
        when(votoMapper.toVotoDTO(any())).thenReturn(
            new com.sicredi.votacao.controllers.response.VotoResponse(
                2L, 1L, "987.***.**-09", "NAO", "2026-01-01"
            )
        );

        RegistrarVotoRequest request = new RegistrarVotoRequest("98765432109", "NAO");

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voto", is("NAO")));
    }

    @Test
    void registrarVotoComCpfVazio() throws Exception {
        RegistrarVotoRequest request = new RegistrarVotoRequest("", "SIM");

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarVotoComOpcaoInvalida() throws Exception {
        RegistrarVotoRequest request = new RegistrarVotoRequest("12345678901", "INVALIDO");

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
