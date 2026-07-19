package com.sicredi.votacao.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.votacao.controllers.request.CriarPautaRequest;
import com.sicredi.votacao.controllers.request.RegistrarVotoRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
class ValidationErrorTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void criarPautaSemTituloRetorna400() throws Exception {
        CriarPautaRequest request = new CriarPautaRequest("", "Descrição");

        mockMvc.perform(post("/api/v1/pautas")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
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
    void registrarVotoComOpcaoInvalidaRetorna400() throws Exception {
        RegistrarVotoRequest request = new RegistrarVotoRequest("12345678901", "TALVEZ");

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
