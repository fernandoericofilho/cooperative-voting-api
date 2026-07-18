package com.sicredi.votacao.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.votacao.controllers.request.CriarPautaRequest;
import com.sicredi.votacao.controllers.request.AbrirSessaoRequest;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.repositories.PautaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PautaRepository pautaRepository;

    @Test
    void criarPautaReturnsCreatedWithPautaDTO() throws Exception {
        CriarPautaRequest request = new CriarPautaRequest("Pauta Teste", "Descrição");

        MvcResult result = mockMvc.perform(post("/api/v1/pautas")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo", is("Pauta Teste")))
                .andExpect(jsonPath("$.descricao", is("Descrição")))
                .andExpect(jsonPath("$.status", is("NAO_INICIADA")))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();
    }

    @Test
    void obterPautaReturnsPautaDTO() throws Exception {
        Pauta pauta = new Pauta("Pauta X", "Desc X");
        Pauta saved = pautaRepository.save(pauta);

        mockMvc.perform(get("/api/v1/pautas/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.titulo", is("Pauta X")))
                .andExpect(jsonPath("$.status", is("NAO_INICIADA")));
    }

    @Test
    void abrirSessaoReturnsPautaDTOWithABERTAStatus() throws Exception {
        Pauta pauta = new Pauta("Pauta Y", "Desc Y");
        Pauta saved = pautaRepository.save(pauta);

        AbrirSessaoRequest request = new AbrirSessaoRequest(60L);

        mockMvc.perform(post("/api/v1/pautas/" + saved.getId() + "/sessoes")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ABERTA")))
                .andExpect(jsonPath("$.sessaoAbertaEm").isNotEmpty());
    }

    @Test
    void obterResultadoReturnsResultadoDTOWithVoteCounts() throws Exception {
        Pauta pauta = new Pauta("Pauta Result", "Desc");
        Pauta saved = pautaRepository.save(pauta);
        saved.abrirSessao(60);
        saved = pautaRepository.save(saved);

        mockMvc.perform(get("/api/v1/pautas/" + saved.getId() + "/resultado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pautaId", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.status", is("ABERTA")))
                .andExpect(jsonPath("$.resultado", is("EMPATE")))
                .andExpect(jsonPath("$.totalSim", is(0)))
                .andExpect(jsonPath("$.totalNao", is(0)));
    }
}
