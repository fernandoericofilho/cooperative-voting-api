package com.sicredi.votacao.enums;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StatusVotacaoTest {

    @Test
    void statusHabilitado() {
        assertThat(StatusVotacao.HABILITADO).isNotNull();
        assertThat(StatusVotacao.HABILITADO.name()).isEqualTo("HABILITADO");
    }

    @Test
    void statusNaoHabilitado() {
        assertThat(StatusVotacao.NAO_HABILITADO).isNotNull();
        assertThat(StatusVotacao.NAO_HABILITADO.name()).isEqualTo("NAO_HABILITADO");
    }
}
