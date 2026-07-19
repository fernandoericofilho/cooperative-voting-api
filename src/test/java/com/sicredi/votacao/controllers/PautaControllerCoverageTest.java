package com.sicredi.votacao.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sicredi.votacao.exceptions.PautaNaoEncontradaException;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.services.PautaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PautaController.class)
class PautaControllerCoverageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PautaService pautaService;

    @MockBean
    private com.sicredi.votacao.mappers.PautaMapper pautaMapper;

    @MockBean
    private com.sicredi.votacao.mappers.ResultadoVotacaoMapper resultadoMapper;

    @Test
    void obterPautaPorIdRetorna200() throws Exception {
        Pauta pauta = new Pauta("Reforma", "Descrição");
        pauta.setId(1L);

        when(pautaService.buscarPorId(1L)).thenReturn(pauta);
        when(pautaMapper.toPautaDTO(any())).thenReturn(
            new com.sicredi.votacao.controllers.response.PautaResponse(
                1L, "Reforma", "Descrição", "2026-01-01", null, null, "NAO_INICIADA"
            )
        );

        mockMvc.perform(get("/api/v1/pautas/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.titulo", is("Reforma")))
            .andExpect(jsonPath("$.descricao", is("Descrição")));
    }

    @Test
    void obterPautaPorIdRetorna404() throws Exception {
        when(pautaService.buscarPorId(999L)).thenThrow(new PautaNaoEncontradaException(999L));

        mockMvc.perform(get("/api/v1/pautas/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void obterPautaPorIdComSessaoAberta() throws Exception {
        Pauta pauta = new Pauta("Votação", "Descrição");
        pauta.setId(2L);
        pauta.abrirSessao(60L);

        when(pautaService.buscarPorId(2L)).thenReturn(pauta);
        when(pautaMapper.toPautaDTO(any())).thenReturn(
            new com.sicredi.votacao.controllers.response.PautaResponse(
                2L, "Votação", "Descrição", "2026-01-01", "2026-01-01T10:00:00", "2026-01-01T10:01:00", "ABERTA"
            )
        );

        mockMvc.perform(get("/api/v1/pautas/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo", is("Votação")))
            .andExpect(jsonPath("$.status", is("ABERTA")));
    }

    @Test
    void obterPautaPorIdVerificaMapperCalled() throws Exception {
        Pauta pauta = new Pauta("Test", "Desc");
        pauta.setId(5L);

        when(pautaService.buscarPorId(5L)).thenReturn(pauta);
        when(pautaMapper.toPautaDTO(any())).thenReturn(
            new com.sicredi.votacao.controllers.response.PautaResponse(
                5L, "Test", "Desc", "2026-01-01", null, null, "NAO_INICIADA"
            )
        );

        mockMvc.perform(get("/api/v1/pautas/5"))
            .andExpect(status().isOk());

        verify(pautaService).buscarPorId(5L);
        verify(pautaMapper).toPautaDTO(any());
    }

    @Test
    void obterPautaPorIdResponseContent() throws Exception {
        Pauta pauta = new Pauta("Lei Nova", "Descrição da lei");
        pauta.setId(10L);

        when(pautaService.buscarPorId(10L)).thenReturn(pauta);
        when(pautaMapper.toPautaDTO(any())).thenReturn(
            new com.sicredi.votacao.controllers.response.PautaResponse(
                10L, "Lei Nova", "Descrição da lei", "2026-01-01", null, null, "NAO_INICIADA"
            )
        );

        mockMvc.perform(get("/api/v1/pautas/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.titulo").value("Lei Nova"))
            .andExpect(jsonPath("$.criadoEm").value("2026-01-01"));
    }
}
