package com.sicredi.votacao.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.votacao.controllers.request.CriarPautaRequest;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.services.PautaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

@WebMvcTest(PautaController.class)
class PautaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PautaService pautaService;

    @MockBean
    private com.sicredi.votacao.mappers.PautaMapper pautaMapper;

    @MockBean
    private com.sicredi.votacao.mappers.ResultadoVotacaoMapper resultadoMapper;

    @Test
    void criarPautaReturns201() throws Exception {
        Pauta pauta = new Pauta("Reforma", "Descrição");
        pauta.setId(1L);

        when(pautaService.criarPauta("Reforma", "Descrição")).thenReturn(pauta);
        when(pautaMapper.toPautaDTO(any())).thenReturn(
            new com.sicredi.votacao.controllers.response.PautaResponse(
                1L, "Reforma", "Descrição", "2026-01-01", null, null, "NAO_INICIADA"
            )
        );

        CriarPautaRequest request = new CriarPautaRequest("Reforma", "Descrição");

        mockMvc.perform(post("/api/v1/pautas")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.titulo", is("Reforma")));
    }

    @Test
    void listarPautasReturns200() throws Exception {
        Pauta pauta1 = new Pauta("Reforma", "Desc1");
        Pauta pauta2 = new Pauta("Lei", "Desc2");

        var page = new PageImpl<>(List.of(pauta1, pauta2), PageRequest.of(0, 10), 2);
        when(pautaService.listarPautas(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/pautas?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void listarPautasVazia() throws Exception {
        var emptyPage = new PageImpl<Pauta>(List.of(), PageRequest.of(0, 10), 0);
        when(pautaService.listarPautas(any())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/pautas?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void abrirSessaoReturns200() throws Exception {
        Pauta pauta = new Pauta("Reforma", "Desc");
        pauta.abrirSessao(60L);

        when(pautaService.abrirSessao(1L, 60L)).thenReturn(pauta);

        mockMvc.perform(post("/api/v1/pautas/1/sessoes")
                .contentType("application/json")
                .content("{\"duracaoSegundos\": 60}"))
                .andExpect(status().isOk());
    }

    @Test
    void apurarResultadoReturns200() throws Exception {
        com.sicredi.votacao.dtos.ResultadoPautaDto resultado =
            com.sicredi.votacao.dtos.ResultadoPautaDto.calcular(5, 3);

        when(pautaService.apurarResultado(1L)).thenReturn(resultado);
        when(resultadoMapper.toResultadoDTO(any(), any())).thenReturn(
            new com.sicredi.votacao.controllers.response.ResultadoVotacaoResponse(
                1L, 5, 3, "APROVADA", null
            )
        );

        mockMvc.perform(get("/api/v1/pautas/1/resultado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSim", is(5)))
                .andExpect(jsonPath("$.totalNao", is(3)));
    }
}
