package com.sicredi.votacao.models;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PautaStatusTest {

    @Test
    void pautaInicial() {
        Pauta pauta = new Pauta("Teste", "Desc");

        assertThat(pauta.sessaoFoiAberta()).isFalse();
        assertThat(pauta.sessaoEstaAberta()).isFalse();
        assertThat(pauta.sessaoEstaEncerrada()).isFalse();
    }

    @Test
    void pautaAberta() {
        Pauta pauta = new Pauta("Teste", "Desc");
        pauta.abrirSessao(120L);

        assertThat(pauta.sessaoFoiAberta()).isTrue();
        assertThat(pauta.sessaoEstaAberta()).isTrue();
        assertThat(pauta.sessaoEstaEncerrada()).isFalse();
    }

    @Test
    void pautaEncerrada() {
        Pauta pauta = new Pauta("Teste", "Desc");
        pauta.abrirSessao(1L);
        try { Thread.sleep(1100); } catch (InterruptedException e) {}

        assertThat(pauta.sessaoFoiAberta()).isTrue();
        assertThat(pauta.sessaoEstaAberta()).isFalse();
        assertThat(pauta.sessaoEstaEncerrada()).isTrue();
    }

    @Test
    void pautaTitulo() {
        Pauta pauta = new Pauta("Meu Título", "Descrição");

        assertThat(pauta.getTitulo()).isEqualTo("Meu Título");
        assertThat(pauta.getDescricao()).isEqualTo("Descrição");
    }

    @Test
    void pautaComID() {
        Pauta pauta = new Pauta("Test", "Desc");
        pauta.setId(999L);

        assertThat(pauta.getId()).isEqualTo(999L);
    }

    @Test
    void votoComCPF() {
        Voto voto = new Voto(1L, "12345678901", com.sicredi.votacao.enums.OpcaoVoto.SIM);

        assertThat(voto.getCpfAssociado()).isEqualTo("12345678901");
        assertThat(voto.getPautaId()).isEqualTo(1L);
    }
}
