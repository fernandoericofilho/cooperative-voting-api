package com.sicredi.votacao.controllers.request;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequestValidationTest {

    @Test
    void criarPautaRequestComValores() {
        CriarPautaRequest req = new CriarPautaRequest("Reforma", "Descrição");

        assertThat(req.titulo()).isEqualTo("Reforma");
        assertThat(req.descricao()).isEqualTo("Descrição");
    }

    @Test
    void abrirSessaoRequestComDuracao() {
        AbrirSessaoRequest req = new AbrirSessaoRequest(120L);

        assertThat(req.duracaoSegundos()).isEqualTo(120L);
    }

    @Test
    void abrirSessaoRequestSemDuracao() {
        AbrirSessaoRequest req = new AbrirSessaoRequest(null);

        assertThat(req.duracaoSegundos()).isNull();
    }

    @Test
    void registrarVotoRequestValido() {
        RegistrarVotoRequest req = new RegistrarVotoRequest("12345678901", "SIM");

        assertThat(req.cpfAssociado()).isEqualTo("12345678901");
        assertThat(req.voto()).isEqualTo("SIM");
    }

    @Test
    void registrarVotoRequestNao() {
        RegistrarVotoRequest req = new RegistrarVotoRequest("98765432109", "NAO");

        assertThat(req.voto()).isEqualTo("NAO");
    }
}
