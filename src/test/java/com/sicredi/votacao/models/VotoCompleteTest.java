package com.sicredi.votacao.models;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.votacao.enums.OpcaoVoto;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class VotoCompleteTest {

    @Test
    void votoDifferentCpfs() {
        Voto v1 = new Voto(1L, "11111111111", OpcaoVoto.SIM);
        Voto v2 = new Voto(1L, "22222222222", OpcaoVoto.SIM);

        assertThat(v1.getCpfAssociado()).isNotEqualTo(v2.getCpfAssociado());
        assertThat(v1.getVoto()).isEqualTo(v2.getVoto());
    }

    @Test
    void votoDifferentPautas() {
        Voto v1 = new Voto(1L, "12345678901", OpcaoVoto.SIM);
        Voto v2 = new Voto(2L, "12345678901", OpcaoVoto.SIM);

        assertThat(v1.getPautaId()).isNotEqualTo(v2.getPautaId());
        assertThat(v1.getCpfAssociado()).isEqualTo(v2.getCpfAssociado());
    }

    @Test
    void votoSIMandNAO() {
        Voto vSim = new Voto(1L, "11111111111", OpcaoVoto.SIM);
        Voto vNao = new Voto(1L, "22222222222", OpcaoVoto.NAO);

        assertThat(vSim.getVoto()).isNotEqualTo(vNao.getVoto());
    }

    @Test
    void votoCriadoEmNotNull() {
        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);

        assertThat(voto.getCriadoEm()).isNotNull();
        assertThat(voto.getCriadoEm()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void votoIdNull() {
        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);

        assertThat(voto.getId()).isNull();
    }

    @Test
    void votoIdSettable() {
        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);
        voto.setId(123L);

        assertThat(voto.getId()).isEqualTo(123L);
    }

    @Test
    void votoCriadoEmSettable() {
        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);
        LocalDateTime before = voto.getCriadoEm();

        LocalDateTime newTime = LocalDateTime.now().plusHours(1);
        voto.setCriadoEm(newTime);

        assertThat(voto.getCriadoEm()).isNotEqualTo(before);
        assertThat(voto.getCriadoEm()).isEqualTo(newTime);
    }
}
