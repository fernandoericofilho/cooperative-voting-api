package com.sicredi.votacao.models;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.votacao.enums.OpcaoVoto;
import org.junit.jupiter.api.Test;

class VotoEdgeCasesTest {

    @Test
    void votoCpfComCaracteresEspeciais() {
        String cpf = "99999999999";
        Voto voto = new Voto(1L, cpf, OpcaoVoto.SIM);

        assertThat(voto.getCpfAssociado()).isEqualTo(cpf);
        assertThat(voto.getCpfAssociado().length()).isEqualTo(11);
    }

    @Test
    void votoPautaIdZero() {
        Voto voto = new Voto(0L, "12345678901", OpcaoVoto.SIM);

        assertThat(voto.getPautaId()).isEqualTo(0L);
    }

    @Test
    void votoPautaIdNegativo() {
        Voto voto = new Voto(-1L, "12345678901", OpcaoVoto.SIM);

        assertThat(voto.getPautaId()).isEqualTo(-1L);
    }

    @Test
    void votoMultipleSetters() {
        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);

        voto.setPautaId(2L);
        assertThat(voto.getPautaId()).isEqualTo(2L);

        voto.setCpfAssociado("98765432109");
        assertThat(voto.getCpfAssociado()).isEqualTo("98765432109");

        voto.setVoto(OpcaoVoto.NAO);
        assertThat(voto.getVoto()).isEqualTo(OpcaoVoto.NAO);
    }

    @Test
    void votoEnumComparison() {
        Voto vSim = new Voto(1L, "11111111111", OpcaoVoto.SIM);
        Voto vNao = new Voto(1L, "11111111111", OpcaoVoto.NAO);

        assertThat(vSim.getVoto()).isNotEqualTo(vNao.getVoto());
        assertThat(OpcaoVoto.SIM).isNotEqualTo(OpcaoVoto.NAO);
    }
}
