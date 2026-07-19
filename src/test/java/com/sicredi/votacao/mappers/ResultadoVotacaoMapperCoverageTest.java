package com.sicredi.votacao.mappers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.votacao.controllers.response.ResultadoVotacaoResponse;
import com.sicredi.votacao.dtos.ResultadoPautaDto;
import com.sicredi.votacao.models.Pauta;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class ResultadoVotacaoMapperCoverageTest {

    private final ResultadoVotacaoMapper mapper = new ResultadoVotacaoMapper();

    @Test
    void mapearResultadoVotacaoPautaNaoIniciada() {
        Pauta pauta = new Pauta("Reforma", "Descrição");
        ResultadoPautaDto resultado = new ResultadoPautaDto(10L, 5L, "APROVADA");

        ResultadoVotacaoResponse response = mapper.toResultadoDTO(pauta, resultado);

        assertThat(response.getPautaId()).isEqualTo(pauta.getId());
        assertThat(response.getTotalSim()).isEqualTo(10);
        assertThat(response.getTotalNao()).isEqualTo(5);
        assertThat(response.getResultado()).isEqualTo("APROVADA");
        assertThat(response.getStatus()).isEqualTo("NAO_INICIADA");
    }

    @Test
    void mapearResultadoVotacaoPautaAberta() {
        Pauta pauta = new Pauta("Reforma", "Descrição");
        pauta.abrirSessao(600L);

        ResultadoPautaDto resultado = new ResultadoPautaDto(8L, 4L, "APROVADA");

        ResultadoVotacaoResponse response = mapper.toResultadoDTO(pauta, resultado);

        assertThat(response.getStatus()).isEqualTo("ABERTA");
        assertThat(response.getTotalSim()).isEqualTo(8);
        assertThat(response.getTotalNao()).isEqualTo(4);
    }

    @Test
    void mapearResultadoVotacaoPautaEncerrada() {
        Pauta pauta = new Pauta("Reforma", "Descrição");
        pauta.abrirSessao(1L);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ResultadoPautaDto resultado = new ResultadoPautaDto(6L, 3L, "APROVADA");

        ResultadoVotacaoResponse response = mapper.toResultadoDTO(pauta, resultado);

        assertThat(response.getStatus()).isEqualTo("ENCERRADA");
        assertThat(response.getTotalSim()).isEqualTo(6);
        assertThat(response.getTotalNao()).isEqualTo(3);
    }

    @Test
    void mapearResultadoComReprovacao() {
        Pauta pauta = new Pauta("Lei Fundamental", "Desc");
        pauta.setId(3L);
        pauta.abrirSessao(60L);

        ResultadoPautaDto resultado = new ResultadoPautaDto(15L, 20L, "REPROVADA");

        ResultadoVotacaoResponse response = mapper.toResultadoDTO(pauta, resultado);

        assertThat(response.getPautaId()).isEqualTo(3L);
        assertThat(response.getTotalSim()).isEqualTo(15);
        assertThat(response.getTotalNao()).isEqualTo(20);
        assertThat(response.getResultado()).isEqualTo("REPROVADA");
    }

    @Test
    void mapearResultadoEmpate() {
        Pauta pauta = new Pauta("Votação", "Empate");
        pauta.abrirSessao(60L);

        ResultadoPautaDto resultado = new ResultadoPautaDto(5L, 5L, "EMPATE");

        ResultadoVotacaoResponse response = mapper.toResultadoDTO(pauta, resultado);

        assertThat(response.getTotalSim()).isEqualTo(5);
        assertThat(response.getTotalNao()).isEqualTo(5);
    }

    @Test
    void mapearResultadoComZeroVotos() {
        Pauta pauta = new Pauta("Votação", "Sem votos");
        pauta.abrirSessao(60L);

        ResultadoPautaDto resultado = new ResultadoPautaDto(0L, 0L, "EMPATE");

        ResultadoVotacaoResponse response = mapper.toResultadoDTO(pauta, resultado);

        assertThat(response.getTotalSim()).isEqualTo(0);
        assertThat(response.getTotalNao()).isEqualTo(0);
    }

    @Test
    void mapearResultadoGettersAllProperties() {
        Pauta pauta = new Pauta("Reforma", "Descrição");
        pauta.setId(4L);
        pauta.abrirSessao(60L);

        ResultadoPautaDto resultado = new ResultadoPautaDto(10L, 5L, "APROVADA");
        ResultadoVotacaoResponse response = mapper.toResultadoDTO(pauta, resultado);

        assertThat(response.getPautaId()).isEqualTo(4L);
        assertThat(response.getTotalSim()).isEqualTo(10);
        assertThat(response.getTotalNao()).isEqualTo(5);
        assertThat(response.getResultado()).isEqualTo("APROVADA");
        assertThat(response.getStatus()).isEqualTo("ABERTA");
    }
}
