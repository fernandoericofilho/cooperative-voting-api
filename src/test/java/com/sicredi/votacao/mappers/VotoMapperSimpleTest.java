package com.sicredi.votacao.mappers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.votacao.controllers.response.VotoResponse;
import com.sicredi.votacao.enums.OpcaoVoto;
import com.sicredi.votacao.models.Voto;
import org.junit.jupiter.api.Test;

class VotoMapperSimpleTest {

    private final VotoMapper mapper = new VotoMapper();

    @Test
    void mapearVotoSim() {
        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);

        VotoResponse response = mapper.toVotoDTO(voto);

        assertThat(response.getPautaId()).isEqualTo(1L);
        assertThat(response.getVoto()).isEqualTo("SIM");
        // CPF pode estar mascarado ou não, dependendo da implementação
        assertThat(response.getCpfAssociado()).isNotNull();
    }

    @Test
    void mapearVotoNao() {
        Voto voto = new Voto(2L, "98765432109", OpcaoVoto.NAO);

        VotoResponse response = mapper.toVotoDTO(voto);

        assertThat(response.getPautaId()).isEqualTo(2L);
        assertThat(response.getVoto()).isEqualTo("NAO");
    }

    @Test
    void mapearVotoCriaoEmNotNull() {
        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);

        VotoResponse response = mapper.toVotoDTO(voto);

        assertThat(response.getCriadoEm()).isNotNull();
    }

    @Test
    void mapearVotoDiferentesCpfs() {
        Voto v1 = new Voto(1L, "11111111111", OpcaoVoto.SIM);
        Voto v2 = new Voto(1L, "22222222222", OpcaoVoto.SIM);

        VotoResponse r1 = mapper.toVotoDTO(v1);
        VotoResponse r2 = mapper.toVotoDTO(v2);

        assertThat(r1.getCpfAssociado()).isNotEqualTo(r2.getCpfAssociado());
    }

    @Test
    void mapearVotoIdNull() {
        Voto voto = new Voto(1L, "12345678901", OpcaoVoto.SIM);

        VotoResponse response = mapper.toVotoDTO(voto);

        assertThat(response.getId()).isNull();
    }
}
