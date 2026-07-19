package com.sicredi.votacao.controllers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.models.Voto;
import org.junit.jupiter.api.Test;

class VotoControllerUnitTest {

    @Test
    void votoRegistrado() {
        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);

        assertThat(voto.getPautaId()).isEqualTo(1L);
        assertThat(voto.getCpfAssociado()).isEqualTo("12345678901");
        assertThat(voto.getVoto()).isEqualTo(OpcaoVoto.SIM);
    }

    @Test
    void votoNao() {
        Voto voto = new Voto(2L, "98765432101", OpcaoVoto.NAO);

        assertThat(voto.getVoto()).isEqualTo(OpcaoVoto.NAO);
    }
}
