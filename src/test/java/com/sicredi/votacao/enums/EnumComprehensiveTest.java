package com.sicredi.votacao.enums;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EnumComprehensiveTest {

    @Test
    void opcaoVotoSIMExists() {
        assertThat(OpcaoVoto.SIM).isNotNull();
        assertThat(OpcaoVoto.SIM.name()).isEqualTo("SIM");
    }

    @Test
    void opcaoVotoNAOExists() {
        assertThat(OpcaoVoto.NAO).isNotNull();
        assertThat(OpcaoVoto.NAO.name()).isEqualTo("NAO");
    }

    @Test
    void statusVotacaoHABILITADOExists() {
        assertThat(StatusVotacao.HABILITADO).isNotNull();
        assertThat(StatusVotacao.HABILITADO.name()).isEqualTo("HABILITADO");
    }

    @Test
    void statusVotacaoNAO_HABILITADOExists() {
        assertThat(StatusVotacao.NAO_HABILITADO).isNotNull();
        assertThat(StatusVotacao.NAO_HABILITADO.name()).isEqualTo("NAO_HABILITADO");
    }

    @Test
    void opcaoVotoValues() {
        OpcaoVoto[] values = OpcaoVoto.values();
        assertThat(values).contains(OpcaoVoto.SIM, OpcaoVoto.NAO);
        assertThat(values.length).isEqualTo(2);
    }

    @Test
    void statusVotacaoValues() {
        StatusVotacao[] values = StatusVotacao.values();
        assertThat(values).contains(StatusVotacao.HABILITADO, StatusVotacao.NAO_HABILITADO);
        assertThat(values.length).isEqualTo(2);
    }

    @Test
    void enumEquality() {
        OpcaoVoto sim1 = OpcaoVoto.SIM;
        OpcaoVoto sim2 = OpcaoVoto.valueOf("SIM");

        assertThat(sim1).isEqualTo(sim2);
    }
}
