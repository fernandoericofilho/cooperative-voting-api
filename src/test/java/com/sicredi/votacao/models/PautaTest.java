package com.sicredi.votacao.models;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class PautaTest {

    @Test
    void sessaoNaoAbertaInicialmente() {
        Pauta pauta = new Pauta("Reforma", "Desc");

        assertThat(pauta.sessaoFoiAberta()).isFalse();
        assertThat(pauta.sessaoEstaAberta()).isFalse();
    }

    @Test
    void abrirSessaoComDuracao() {
        Pauta pauta = new Pauta("Reforma", "Desc");

        pauta.abrirSessao(60);

        assertThat(pauta.sessaoFoiAberta()).isTrue();
        assertThat(pauta.sessaoEstaAberta()).isTrue();
        assertThat(pauta.getSessaoAbertaEm()).isNotNull();
        assertThat(pauta.getSessaoFechaEm()).isNotNull();
    }

    @Test
    void sessaoDuracaoPadraoEhSessenta() {
        Pauta pauta = new Pauta("Reforma", "Desc");

        pauta.abrirSessao(60L);

        LocalDateTime fechaEm = pauta.getSessaoFechaEm();
        LocalDateTime abertaEm = pauta.getSessaoAbertaEm();
        long secondsDifference = java.time.temporal.ChronoUnit.SECONDS.between(abertaEm, fechaEm);

        assertThat(secondsDifference).isEqualTo(60);
    }

    @Test
    void sessaoEncerradaAposExpiracao() throws InterruptedException {
        Pauta pauta = new Pauta("Reforma", "Desc");
        pauta.abrirSessao(1);

        Thread.sleep(1100);

        assertThat(pauta.sessaoEstaEncerrada()).isTrue();
        assertThat(pauta.sessaoEstaAberta()).isFalse();
    }
}
