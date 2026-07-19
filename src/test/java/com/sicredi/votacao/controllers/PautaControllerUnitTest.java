package com.sicredi.votacao.controllers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.votacao.models.Pauta;
import org.junit.jupiter.api.Test;

class PautaControllerUnitTest {

    @Test
    void pautaResponseMapping() {
        Pauta pauta = new Pauta("Reforma", "Desc");

        assertThat(pauta.getTitulo()).isEqualTo("Reforma");
        assertThat(pauta.getDescricao()).isEqualTo("Desc");
    }

    @Test
    void pautaComSessaoAberta() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        pauta.abrirSessao(60L);

        assertThat(pauta.sessaoEstaAberta()).isTrue();
    }
}
