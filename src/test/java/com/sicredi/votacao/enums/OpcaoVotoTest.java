package com.sicredi.votacao.enums;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OpcaoVotoTest {

    @Test
    void opcaoVotoSIM() {
        assertThat(OpcaoVoto.SIM).isNotNull();
        assertThat(OpcaoVoto.SIM.name()).isEqualTo("SIM");
    }

    @Test
    void opcaoVotoNAO() {
        assertThat(OpcaoVoto.NAO).isNotNull();
        assertThat(OpcaoVoto.NAO.name()).isEqualTo("NAO");
    }
}
