package com.sicredi.votacao.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.votacao.controllers.request.CpfFormularioRequest;
import com.sicredi.votacao.services.PautaService;
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
class VotoTelaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PautaService pautaService;

    @Test
    void telaDeVotoPedeCpf() throws Exception {
        var pauta = pautaService.criarPauta("Pauta Tela Voto", "desc");

        mockMvc.perform(post("/api/v1/pautas/" + pauta.getId() + "/votos/tela"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo", equalTo("FORMULARIO")))
            .andExpect(jsonPath("$.botaoOk.url", equalTo("/api/v1/pautas/" + pauta.getId() + "/votos/opcoes")));
    }

    @Test
    void opcoesDeVotoRetornaSelecaoSimNaoComCpfNoBody() throws Exception {
        var pauta = pautaService.criarPauta("Pauta Opcoes", "desc");

        mockMvc.perform(post("/api/v1/pautas/" + pauta.getId() + "/votos/opcoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CpfFormularioRequest("11122233344"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo", equalTo("SELECAO")))
            .andExpect(jsonPath("$.itens.length()", equalTo(2)))
            .andExpect(jsonPath("$.itens[0].body.cpfAssociado", equalTo("11122233344")));
    }
}
