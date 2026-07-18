package com.sicredi.votacao.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.votacao.controllers.request.RegistrarVotoRequest;
import com.sicredi.votacao.enums.StatusVotacao;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.repositories.PautaRepository;
import com.sicredi.votacao.services.external.UserInfoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.profiles.active=test"})
class VotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PautaRepository pautaRepository;

    @MockBean
    private UserInfoClient userInfoClient;

    private Pauta pautaAberta;

    @BeforeEach
    void setup() {
        Pauta pauta = new Pauta("Pauta Teste", "Desc");
        pautaAberta = pautaRepository.save(pauta);
        pautaAberta.abrirSessao(60);
        pautaAberta = pautaRepository.save(pautaAberta);
    }

    @Test
    void registrarVotoReturnsVotoDTOWithCreatedStatus() throws Exception {
        when(userInfoClient.consultar("12345678901")).thenReturn(StatusVotacao.ABLE_TO_VOTE);

        RegistrarVotoRequest request = new RegistrarVotoRequest("12345678901", "SIM");

        mockMvc.perform(post("/api/v1/pautas/" + pautaAberta.getId() + "/votos")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pautaId", is(pautaAberta.getId().intValue())))
                .andExpect(jsonPath("$.cpfAssociado", is("12345678901")))
                .andExpect(jsonPath("$.voto", is("SIM")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.criadoEm").isNotEmpty());
    }
}
