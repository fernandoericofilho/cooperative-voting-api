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

import org.junit.jupiter.api.BeforeEach;

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

    @BeforeEach
    void setUp() {
        pautaRepository.deleteAll();
    }

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

    @Test
    void listarPautasRetornaComPaginacaoPadrao() throws Exception {
        // Create 15 pautas
        for (int i = 0; i < 15; i++) {
            pautaRepository.save(new Pauta("Pauta " + i, "Descrição " + i));
        }

        mockMvc.perform(get("/api/v1/pautas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(10)))
                .andExpect(jsonPath("$.totalElements", is(15)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    void listarPautasRetornaPrimeiraPageComTamanhoPersonalizado() throws Exception {
        // Create 15 pautas
        for (int i = 0; i < 15; i++) {
            pautaRepository.save(new Pauta("Pauta " + i, "Descrição " + i));
        }

        mockMvc.perform(get("/api/v1/pautas?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(5)))
                .andExpect(jsonPath("$.totalElements", is(15)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(5)));
    }

    @Test
    void listarPautasRetornaSegundaPage() throws Exception {
        // Create 15 pautas
        for (int i = 0; i < 15; i++) {
            pautaRepository.save(new Pauta("Pauta " + i, "Descrição " + i));
        }

        mockMvc.perform(get("/api/v1/pautas?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(5)))
                .andExpect(jsonPath("$.totalElements", is(15)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.number", is(1)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    void listarPautasRetornaEmOrdenacaoPersonalizada() throws Exception {
        Pauta pauta1 = pautaRepository.save(new Pauta("Pauta 1", "Descrição 1"));
        Pauta pauta2 = pautaRepository.save(new Pauta("Pauta 2", "Descrição 2"));
        Pauta pauta3 = pautaRepository.save(new Pauta("Pauta 3", "Descrição 3"));

        mockMvc.perform(get("/api/v1/pautas?page=0&size=10&sortBy=titulo&sortDirection=ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(3)))
                .andExpect(jsonPath("$.content[0].titulo", is("Pauta 1")))
                .andExpect(jsonPath("$.content[1].titulo", is("Pauta 2")))
                .andExpect(jsonPath("$.content[2].titulo", is("Pauta 3")));
    }

    @Test
    void listarPautasRetornaVazioQuandoNaoExistemPautas() throws Exception {
        mockMvc.perform(get("/api/v1/pautas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(0)))
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.totalPages", is(0)))
                .andExpect(jsonPath("$.number", is(0)));
    }
}
