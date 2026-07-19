package com.sicredi.votacao.models;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class PautaCompleteTest {

    @Test
    void pautaTituloDescricao() {
        Pauta pauta = new Pauta("Titulo Longo", "Descrição Detalhada");

        assertThat(pauta.getTitulo()).isEqualTo("Titulo Longo");
        assertThat(pauta.getDescricao()).isEqualTo("Descrição Detalhada");
    }

    @Test
    void pautaCriadoEmNotNull() {
        Pauta pauta = new Pauta("Test", "Desc");

        assertThat(pauta.getCriadaEm()).isNotNull();
        assertThat(pauta.getCriadaEm()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void pautaIdNull() {
        Pauta pauta = new Pauta("Test", "Desc");

        assertThat(pauta.getId()).isNull();
    }

    @Test
    void pautaIdSettable() {
        Pauta pauta = new Pauta("Test", "Desc");
        pauta.setId(999L);

        assertThat(pauta.getId()).isEqualTo(999L);
    }

    @Test
    void pautaSessaoFieldsNullInitially() {
        Pauta pauta = new Pauta("Test", "Desc");

        assertThat(pauta.getSessaoAbertaEm()).isNull();
        assertThat(pauta.getSessaoFechaEm()).isNull();
    }

    @Test
    void pautaAbrirSessaoSetsTimestamps() {
        Pauta pauta = new Pauta("Test", "Desc");
        LocalDateTime before = LocalDateTime.now();

        pauta.abrirSessao(60L);

        assertThat(pauta.getSessaoAbertaEm()).isNotNull();
        assertThat(pauta.getSessaoFechaEm()).isNotNull();
        assertThat(pauta.getSessaoAbertaEm()).isAfterOrEqualTo(before);
        assertThat(pauta.getSessaoFechaEm()).isAfter(pauta.getSessaoAbertaEm());
    }

    @Test
    void pautaDuracaoZero() {
        Pauta pauta = new Pauta("Test", "Desc");
        pauta.abrirSessao(0L);

        assertThat(pauta.getSessaoFechaEm()).isEqualTo(pauta.getSessaoAbertaEm());
    }

    @Test
    void pautaDuracaoLonga() {
        Pauta pauta = new Pauta("Test", "Desc");
        pauta.abrirSessao(86400L);

        assertThat(pauta.getSessaoFechaEm()).isAfter(pauta.getSessaoAbertaEm().plusHours(23));
    }

    @Test
    void pautaTituloSettable() {
        Pauta pauta = new Pauta("Original", "Desc");
        pauta.setTitulo("Novo Titulo");

        assertThat(pauta.getTitulo()).isEqualTo("Novo Titulo");
    }

    @Test
    void pautaDescricaoSettable() {
        Pauta pauta = new Pauta("Test", "Original");
        pauta.setDescricao("Nova Descrição");

        assertThat(pauta.getDescricao()).isEqualTo("Nova Descrição");
    }

    @Test
    void pautaCriadoEmSettable() {
        Pauta pauta = new Pauta("Test", "Desc");
        LocalDateTime before = pauta.getCriadaEm();

        LocalDateTime newTime = before.minusHours(1);
        pauta.setCriadaEm(newTime);

        assertThat(pauta.getCriadaEm()).isNotEqualTo(before);
        assertThat(pauta.getCriadaEm()).isEqualTo(newTime);
    }
}
