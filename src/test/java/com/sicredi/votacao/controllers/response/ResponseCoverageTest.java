package com.sicredi.votacao.controllers.response;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ResponseCoverageTest {

    @Test
    void pautaResponseAllGetters() {
        PautaResponse response = new PautaResponse(
            1L, "Reforma", "Descrição", "2026-01-01T10:00:00",
            "2026-01-01T10:00:00", "2026-01-01T10:01:00", "ABERTA"
        );

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitulo()).isEqualTo("Reforma");
        assertThat(response.getDescricao()).isEqualTo("Descrição");
        assertThat(response.getCriadoEm()).isEqualTo("2026-01-01T10:00:00");
        assertThat(response.getSessaoAbertaEm()).isEqualTo("2026-01-01T10:00:00");
        assertThat(response.getSessaoFechaEm()).isEqualTo("2026-01-01T10:01:00");
        assertThat(response.getStatus()).isEqualTo("ABERTA");
    }

    @Test
    void pautaResponseWithNullValues() {
        PautaResponse response = new PautaResponse(
            2L, "Lei", "Desc Lei", "2026-01-01T10:00:00", null, null, "NAO_INICIADA"
        );

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getTitulo()).isEqualTo("Lei");
        assertThat(response.getSessaoAbertaEm()).isNull();
        assertThat(response.getSessaoFechaEm()).isNull();
        assertThat(response.getStatus()).isEqualTo("NAO_INICIADA");
    }

    @Test
    void votoResponseAllGetters() {
        VotoResponse response = new VotoResponse(
            1L, 1L, "12345678901", "SIM", "2026-01-01T10:00:00"
        );

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPautaId()).isEqualTo(1L);
        assertThat(response.getCpfAssociado()).isEqualTo("12345678901");
        assertThat(response.getVoto()).isEqualTo("SIM");
        assertThat(response.getCriadoEm()).isEqualTo("2026-01-01T10:00:00");
    }

    @Test
    void votoResponseWithDifferentVoto() {
        VotoResponse response = new VotoResponse(
            2L, 2L, "98765432100", "NAO", "2026-01-01T11:00:00"
        );

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getPautaId()).isEqualTo(2L);
        assertThat(response.getCpfAssociado()).isEqualTo("98765432100");
        assertThat(response.getVoto()).isEqualTo("NAO");
        assertThat(response.getCriadoEm()).isEqualTo("2026-01-01T11:00:00");
    }

    @Test
    void resultadoVotacaoResponseAllGetters() {
        ResultadoVotacaoResponse response = new ResultadoVotacaoResponse(
            1L, 10, 5, "APROVADO", "ENCERRADA"
        );

        assertThat(response.getPautaId()).isEqualTo(1L);
        assertThat(response.getTotalSim()).isEqualTo(10);
        assertThat(response.getTotalNao()).isEqualTo(5);
        assertThat(response.getResultado()).isEqualTo("APROVADO");
        assertThat(response.getStatus()).isEqualTo("ENCERRADA");
    }

    @Test
    void resultadoVotacaoResponseReprovado() {
        ResultadoVotacaoResponse response = new ResultadoVotacaoResponse(
            2L, 3, 8, "REPROVADO", "ENCERRADA"
        );

        assertThat(response.getPautaId()).isEqualTo(2L);
        assertThat(response.getTotalSim()).isEqualTo(3);
        assertThat(response.getTotalNao()).isEqualTo(8);
        assertThat(response.getResultado()).isEqualTo("REPROVADO");
        assertThat(response.getStatus()).isEqualTo("ENCERRADA");
    }

    @Test
    void pautaResponseConstructorCoverage() {
        PautaResponse response = new PautaResponse(
            5L, "Test Pauta", "Test Description", "2026-01-01T12:00:00",
            "2026-01-01T12:00:00", "2026-01-01T13:00:00", "ABERTA"
        );

        assertThat(response)
            .hasFieldOrPropertyWithValue("id", 5L)
            .hasFieldOrPropertyWithValue("titulo", "Test Pauta")
            .hasFieldOrPropertyWithValue("descricao", "Test Description")
            .hasFieldOrPropertyWithValue("criadoEm", "2026-01-01T12:00:00")
            .hasFieldOrPropertyWithValue("sessaoAbertaEm", "2026-01-01T12:00:00")
            .hasFieldOrPropertyWithValue("sessaoFechaEm", "2026-01-01T13:00:00")
            .hasFieldOrPropertyWithValue("status", "ABERTA");
    }

    @Test
    void votoResponseConstructorCoverage() {
        VotoResponse response = new VotoResponse(
            10L, 5L, "11122233344", "SIM", "2026-01-01T14:00:00"
        );

        assertThat(response)
            .hasFieldOrPropertyWithValue("id", 10L)
            .hasFieldOrPropertyWithValue("pautaId", 5L)
            .hasFieldOrPropertyWithValue("cpfAssociado", "11122233344")
            .hasFieldOrPropertyWithValue("voto", "SIM")
            .hasFieldOrPropertyWithValue("criadoEm", "2026-01-01T14:00:00");
    }

    @Test
    void resultadoVotacaoResponseConstructorCoverage() {
        ResultadoVotacaoResponse response = new ResultadoVotacaoResponse(
            20L, 15, 10, "APROVADO", "ABERTA"
        );

        assertThat(response)
            .hasFieldOrPropertyWithValue("pautaId", 20L)
            .hasFieldOrPropertyWithValue("totalSim", 15)
            .hasFieldOrPropertyWithValue("totalNao", 10)
            .hasFieldOrPropertyWithValue("resultado", "APROVADO")
            .hasFieldOrPropertyWithValue("status", "ABERTA");
    }

    @Test
    void pautasListResponseConstructorAndAllGetters() {
        List<PautaResponse> content = List.of(
            new PautaResponse(1L, "Pauta1", "Desc1", "2026-01-01T10:00:00",
                              "2026-01-01T10:00:00", "2026-01-01T11:00:00", "ABERTA")
        );

        PautasListResponse response = new PautasListResponse(
            content, 100L, 5, 1, 20, true, false
        );

        assertThat(response.getContent()).isEqualTo(content);
        assertThat(response.getTotalElements()).isEqualTo(100L);
        assertThat(response.getTotalPages()).isEqualTo(5);
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getPageSize()).isEqualTo(20);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.isHasPrevious()).isFalse();
    }

    @Test
    void pautasListResponseWithMultiplePautas() {
        List<PautaResponse> content = List.of(
            new PautaResponse(1L, "Pauta1", "Desc1", "2026-01-01T10:00:00",
                              "2026-01-01T10:00:00", "2026-01-01T11:00:00", "ABERTA"),
            new PautaResponse(2L, "Pauta2", "Desc2", "2026-01-01T10:00:00",
                              "2026-01-01T10:00:00", "2026-01-01T11:00:00", "ENCERRADA")
        );

        PautasListResponse response = new PautasListResponse(
            content, 200L, 10, 2, 20, true, true
        );

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(200L);
        assertThat(response.getCurrentPage()).isEqualTo(2);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.isHasPrevious()).isTrue();
    }

    @Test
    void pageResponseConstructor() {
        List<PautaResponse> content = List.of(
            new PautaResponse(1L, "Pauta1", "Desc1", "2026-01-01T10:00:00",
                              "2026-01-01T10:00:00", "2026-01-01T11:00:00", "ABERTA")
        );

        PageResponse<PautaResponse> response = new PageResponse<>(
            content, 50L, 3, 1, 20, true, false
        );

        assertThat(response.content()).isEqualTo(content);
        assertThat(response.totalElements()).isEqualTo(50L);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.currentPage()).isEqualTo(1);
        assertThat(response.pageSize()).isEqualTo(20);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.hasPrevious()).isFalse();
    }
}
