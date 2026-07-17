package com.sicredi.votacao.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.votacao.controllers.request.AbrirSessaoRequest;
import com.sicredi.votacao.controllers.request.CriarPautaRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void criarPautaRetornaTelaFormularioDeDetalhe() throws Exception {
        var request = new CriarPautaRequest("Reforma do estatuto", "desc");

        mockMvc.perform(post("/api/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo", equalTo("FORMULARIO")))
            .andExpect(jsonPath("$.botaoOk.texto", equalTo("Abrir Sessão")));
    }

    @Test
    void abrirSessaoRetornaTelaComBotaoDeVotar() throws Exception {
        String body = mockMvc.perform(post("/api/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CriarPautaRequest("Pauta 2", "desc"))))
            .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(body).at("/botaoOk/body/id").isMissingNode()
            ? extractIdFromBotaoUrl(objectMapper.readTree(body).at("/botaoOk/url").asText())
            : objectMapper.readTree(body).at("/botaoOk/body/id").asLong();

        mockMvc.perform(post("/api/v1/pautas/" + id + "/sessoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AbrirSessaoRequest(60L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.botaoOk.texto", equalTo("Votar")));
    }

    private Long extractIdFromBotaoUrl(String url) {
        // url shape produced by PautaTelaMapper: /api/v1/pautas/{id}/sessoes/tela
        String[] parts = url.split("/");
        return Long.valueOf(parts[parts.length - 3]);
    }
}
