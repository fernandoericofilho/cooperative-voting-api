package com.sicredi.votacao.mappers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.votacao.controllers.response.VotoResponse;
import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.models.Voto;
import org.junit.jupiter.api.Test;

class VotoMapperTest {

    private final VotoMapper mapper = new VotoMapper();

    @Test
    void mapVotoToVotoResponse() {
        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);

        VotoResponse response = mapper.toVotoDTO(voto);

        assertThat(response.getPautaId()).isEqualTo(1L);
        assertThat(response.getCpfAssociado()).isEqualTo("12345678901");
        assertThat(response.getVoto()).isEqualTo("SIM");
        assertThat(response.getCriadoEm()).isNotNull();
    }

    @Test
    void votoNao() {
        Voto voto = new Voto(1L, "98765432109", OpcaoVoto.NAO);

        VotoResponse response = mapper.toVotoDTO(voto);

        assertThat(response.getVoto()).isEqualTo("NAO");
    }

}
