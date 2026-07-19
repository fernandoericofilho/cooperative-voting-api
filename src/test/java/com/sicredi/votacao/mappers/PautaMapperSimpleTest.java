package com.sicredi.votacao.mappers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.votacao.controllers.response.PautaResponse;
import com.sicredi.votacao.models.Pauta;
import org.junit.jupiter.api.Test;

class PautaMapperSimpleTest {

    private final PautaMapper mapper = new PautaMapper();

    @Test
    void mapearPautaNaoIniciada() {
        Pauta pauta = new Pauta("Titulo", "Descricao");

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getTitulo()).isEqualTo("Titulo");
        assertThat(response.getDescricao()).isEqualTo("Descricao");
        assertThat(response.getStatus()).isEqualTo("NAO_INICIADA");
        assertThat(response.getSessaoAbertaEm()).isNull();
        assertThat(response.getSessaoFechaEm()).isNull();
    }

    @Test
    void mapearPautaEncerrada() {
        Pauta pauta = new Pauta("Titulo", "Descricao");
        pauta.abrirSessao(1L);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
        }

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getStatus()).isEqualTo("ENCERRADA");
        assertThat(response.getSessaoFechaEm()).isNotNull();
    }

    @Test
    void mapearPautaComId() {
        Pauta pauta = new Pauta("Titulo", "Descricao");
        pauta.setId(123L);

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getId()).isEqualTo(123L);
    }

    @Test
    void mapearPautaCriadoEmNotNull() {
        Pauta pauta = new Pauta("Titulo", "Descricao");

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getCriadoEm()).isNotNull();
    }
}
