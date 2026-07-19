package com.sicredi.votacao.mappers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.votacao.controllers.response.PautaResponse;
import com.sicredi.votacao.models.Pauta;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class PautaMapperTest {

    private final PautaMapper mapper = new PautaMapper();

    @Test
    void mapPautaToPautaResponse() {
        Pauta pauta = new Pauta("Reforma", "Desc");
        pauta.abrirSessao(60);

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitulo()).isEqualTo("Reforma");
        assertThat(response.getDescricao()).isEqualTo("Desc");
        assertThat(response.getStatus()).isEqualTo("ABERTA");
    }

    @Test
    void statusNaoIniciada() {
        Pauta pauta = new Pauta("Reforma", "Desc");

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getStatus()).isEqualTo("NAO_INICIADA");
    }

    @Test
    void timestamps() {
        Pauta pauta = new Pauta("Reforma", "Desc");

        PautaResponse response = mapper.toPautaDTO(pauta);

        assertThat(response.getCriadoEm()).isNotNull();
    }
}
