package com.sicredi.votacao.controllers;

import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.repositories.PautaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
class ErrorScenarioTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PautaRepository pautaRepository;

    @BeforeEach
    void setUp() {
        pautaRepository.deleteAll();
    }

    @Test
    void obterPautaInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/v1/pautas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void abrirSessaoEmPautaInexistenteRetorna404() throws Exception {
        String request = "{\"duracaoSegundos\": 60}";
        mockMvc.perform(post("/api/v1/pautas/999/sessoes")
                .contentType("application/json")
                .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrarVotoEmPautaInexistenteRetorna404() throws Exception {
        String request = "{\"cpfAssociado\": \"12345678901\", \"voto\": \"SIM\"}";
        mockMvc.perform(post("/api/v1/pautas/999/votos")
                .contentType("application/json")
                .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void abrirSessaoDuasVezesRetorna409() throws Exception {
        Pauta pauta = pautaRepository.save(new Pauta("Teste", "Desc"));
        String request = "{\"duracaoSegundos\": 60}";

        // Abre primeira vez
        mockMvc.perform(post("/api/v1/pautas/" + pauta.getId() + "/sessoes")
                .contentType("application/json")
                .content(request))
                .andExpect(status().isOk());

        // Tenta abrir segunda vez
        mockMvc.perform(post("/api/v1/pautas/" + pauta.getId() + "/sessoes")
                .contentType("application/json")
                .content(request))
                .andExpect(status().isConflict());
    }
}
