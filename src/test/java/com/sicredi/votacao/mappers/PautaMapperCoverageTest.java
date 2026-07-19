package com.sicredi.votacao.mappers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.votacao.controllers.response.PautaResponse;
import com.sicredi.votacao.models.Pauta;
import org.junit.jupiter.api.Test;

class PautaMapperCoverageTest {

    private final PautaMapper mapper = new PautaMapper();

    @Test
    void mapearPautaAberta() {
        Pauta pauta = new Pauta("Reforma", "Descrição");
        pauta.abrirSessao(600L);

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getStatus()).isEqualTo("ABERTA");
        assertThat(response.getSessaoAbertaEm()).isNotNull();
        assertThat(response.getSessaoFechaEm()).isNotNull();
    }

    @Test
    void mapearPautaAbertatitulo() {
        Pauta pauta = new Pauta("Lei Fundamental", "Descrição da lei");
        pauta.abrirSessao(60L);

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getTitulo()).isEqualTo("Lei Fundamental");
        assertThat(response.getDescricao()).isEqualTo("Descrição da lei");
        assertThat(response.getStatus()).isEqualTo("ABERTA");
    }

    @Test
    void mapearPautaAbertaWithId() {
        Pauta pauta = new Pauta("Votação", "Descrição");
        pauta.setId(100L);
        pauta.abrirSessao(1000L);

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo("ABERTA");
    }

    @Test
    void mapearPautaAbertasessaoInfo() {
        Pauta pauta = new Pauta("Reforma", "Descrição");
        pauta.abrirSessao(500L);

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getCriadoEm()).isNotNull();
        assertThat(response.getSessaoAbertaEm()).isNotNull();
        assertThat(response.getSessaoFechaEm()).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ABERTA");
    }

    @Test
    void mapearPautaAbertaFormatting() {
        Pauta pauta = new Pauta("Test", "Test Desc");
        pauta.abrirSessao(60L);

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getCriadoEm()).isNotNull();
        assertThat(response.getSessaoAbertaEm()).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ABERTA");
    }

    @Test
    void mapearPautaAbertaTimeFormats() {
        Pauta pauta = new Pauta("Lei", "Desc");
        pauta.abrirSessao(120L);

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getStatus()).isEqualTo("ABERTA");
        assertThat(response.getSessaoFechaEm()).isNotBlank();
    }

    @Test
    void mapearPautaAbertaAllFields() {
        Pauta pauta = new Pauta("Votação Final", "Descrição Final");
        pauta.setId(99L);
        pauta.abrirSessao(180L);

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getTitulo()).isEqualTo("Votação Final");
        assertThat(response.getDescricao()).isEqualTo("Descrição Final");
        assertThat(response.getStatus()).isEqualTo("ABERTA");
    }

    @Test
    void mapearPautaAbertatStatusConsistency() {
        Pauta pauta = new Pauta("Teste", "Teste Desc");
        pauta.abrirSessao(60L);

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getStatus())
            .isIn("ABERTA", "ENCERRADA", "NAO_INICIADA")
            .isEqualTo("ABERTA");
    }
}
