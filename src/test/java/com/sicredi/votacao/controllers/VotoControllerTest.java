package com.sicredi.votacao.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.votacao.controllers.request.RegistrarVotoRequest;
import com.sicredi.votacao.dtos.StatusVotacao;
import com.sicredi.votacao.services.PautaService;
import com.sicredi.votacao.services.external.UserInfoClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class VotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PautaService pautaService;

    @MockBean
    private UserInfoClient userInfoClient;

    @Test
    void registrarVotoValidoRetornaConfirmacao() throws Exception {
        var pauta = pautaService.criarPauta("Pauta Voto", "desc");
        pautaService.abrirSessao(pauta.getId(), 60L);
        when(userInfoClient.consultar("11122233344")).thenReturn(StatusVotacao.ABLE_TO_VOTE);

        mockMvc.perform(post("/api/v1/pautas/" + pauta.getId() + "/votos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegistrarVotoRequest("11122233344", "SIM"))))
            .andExpect(status().isOk());
    }
}
