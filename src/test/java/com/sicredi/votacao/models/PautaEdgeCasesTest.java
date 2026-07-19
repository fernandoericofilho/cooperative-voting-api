package com.sicredi.votacao.models;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class PautaEdgeCasesTest {

    @Test
    void pautaTituloMuitoLongo() {
        String tituloLongo = "A".repeat(200);
        Pauta pauta = new Pauta(tituloLongo, "Desc");

        assertThat(pauta.getTitulo()).isEqualTo(tituloLongo);
    }

    @Test
    void pautaDescricaoMuitoLonga() {
        String descricaoLonga = "B".repeat(2000);
        Pauta pauta = new Pauta("Titulo", descricaoLonga);

        assertThat(pauta.getDescricao()).isEqualTo(descricaoLonga);
    }

    @Test
    void pautaAbrirSessaoDurationMuitoAlta() {
        Pauta pauta = new Pauta("Test", "Desc");
        pauta.abrirSessao(86400L);

        assertThat(pauta.getSessaoFechaEm()).isAfter(pauta.getSessaoAbertaEm());
    }

    @Test
    void pautaCriadoEmAndSessaoAbertaEmDifferent() {
        Pauta pauta = new Pauta("Test", "Desc");
        LocalDateTime criadoEm = pauta.getCriadaEm();

        pauta.abrirSessao(120L);
        LocalDateTime sessaoAbertaEm = pauta.getSessaoAbertaEm();

        assertThat(sessaoAbertaEm).isAfterOrEqualTo(criadoEm);
    }

    @Test
    void pautaMultiplasVezesAbrir() {
        Pauta pauta = new Pauta("Test", "Desc");
        pauta.abrirSessao(60L);
        LocalDateTime primeiraAbertura = pauta.getSessaoAbertaEm();

        pauta.abrirSessao(120L);
        LocalDateTime segundaAbertura = pauta.getSessaoAbertaEm();

        assertThat(segundaAbertura).isAfterOrEqualTo(primeiraAbertura);
    }
}
