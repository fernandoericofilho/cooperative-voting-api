package com.sicredi.votacao.exceptions;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sicredi.votacao.controllers.PautaController;
import com.sicredi.votacao.services.PautaService;
import com.sicredi.votacao.services.VotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PautaController.class)
class GlobalExceptionHandlerExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PautaService pautaService;

    @MockBean
    private com.sicredi.votacao.mappers.PautaMapper pautaMapper;

    @MockBean
    private com.sicredi.votacao.mappers.ResultadoVotacaoMapper resultadoMapper;

    // PautaNaoEncontradaException -> 404 NOT_FOUND
    @Test
    void handlePautaNaoEncontradaRetorna404() throws Exception {
        when(pautaService.buscarPorId(999L))
            .thenThrow(new PautaNaoEncontradaException(999L));

        mockMvc.perform(get("/api/v1/pautas/999/resultado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Pauta")));
    }

    // SessaoJaAbertaException -> 409 CONFLICT
    @Test
    void handleSessaoJaAbertaRetorna409() throws Exception {
        when(pautaService.abrirSessao(1L, 60L))
            .thenThrow(new SessaoJaAbertaException(1L));

        mockMvc.perform(post("/api/v1/pautas/1/sessoes")
                .contentType("application/json")
                .content("{\"duracaoSegundos\": 60}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Sessão")));
    }

    // SessaoNaoAbertaException -> 422 UNPROCESSABLE_ENTITY
    @Test
    void handleSessaoNaoAbertaRetorna422() throws Exception {
        when(pautaService.abrirSessao(1L, 60L))
            .thenThrow(new SessaoNaoAbertaException(1L));

        mockMvc.perform(post("/api/v1/pautas/1/sessoes")
                .contentType("application/json")
                .content("{\"duracaoSegundos\": 60}"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message", containsString("Sessão")));
    }

    // SessaoEncerradaException -> 422 UNPROCESSABLE_ENTITY
    @Test
    void handleSessaoEncerradaRetorna422() throws Exception {
        when(pautaService.abrirSessao(1L, 1L))
            .thenThrow(new SessaoEncerradaException(1L));

        mockMvc.perform(post("/api/v1/pautas/1/sessoes")
                .contentType("application/json")
                .content("{\"duracaoSegundos\": 1}"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message", containsString("Sessão")));
    }

    // MethodArgumentNotValidException -> 400 BAD_REQUEST com validação
    @Test
    void handleValidationErrorRetorna400WithDetails() throws Exception {
        mockMvc.perform(post("/api/v1/pautas")
                .contentType("application/json")
                .content("{\"titulo\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Validação")));
    }

    // Exception genérica -> 500 INTERNAL_SERVER_ERROR
    @Test
    void handleGeneralExceptionRetorna500() throws Exception {
        when(pautaService.buscarPorId(1L))
            .thenThrow(new RuntimeException("Erro não esperado"));

        mockMvc.perform(get("/api/v1/pautas/1/resultado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("interno")));
    }

    // VotoDuplicadoException -> 409 CONFLICT
    @Test
    void handleVotoDuplicadoRetorna409() throws Exception {
        when(pautaService.apurarResultado(1L))
            .thenThrow(new VotoDuplicadoException(1L, "12345678901"));

        mockMvc.perform(get("/api/v1/pautas/1/resultado"))
                .andExpect(status().isConflict());
    }

    // AssociadoNaoHabilitadoException -> 422 UNPROCESSABLE_ENTITY
    @Test
    void handleAssociadoNaoHabilitadoRetorna422() throws Exception {
        when(pautaService.apurarResultado(1L))
            .thenThrow(new AssociadoNaoHabilitadoException("12345678901"));

        mockMvc.perform(get("/api/v1/pautas/1/resultado"))
                .andExpect(status().is(422));
    }
}
