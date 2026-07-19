package com.sicredi.votacao.models;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.votacao.enums.OpcaoVoto;
import org.junit.jupiter.api.Test;

class VotoTest {

    @Test
    void criarVotoComPautaIdeCpf() {
        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);

        assertThat(voto.getPautaId()).isEqualTo(1L);
        assertThat(voto.getCpfAssociado()).isEqualTo("12345678901");
        assertThat(voto.getVoto()).isEqualTo(OpcaoVoto.SIM);
        assertThat(voto.getCriadoEm()).isNotNull();
    }

    @Test
    void votoSimOuNao() {
        Voto votoSim = new Voto(1L, "12345678901", OpcaoVoto.SIM);
        Voto votoNao = new Voto(1L, "98765432109", OpcaoVoto.NAO);

        assertThat(votoSim.getVoto()).isEqualTo(OpcaoVoto.SIM);
        assertThat(votoNao.getVoto()).isEqualTo(OpcaoVoto.NAO);
    }
}
