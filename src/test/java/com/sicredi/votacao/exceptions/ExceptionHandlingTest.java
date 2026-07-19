package com.sicredi.votacao.exceptions;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExceptionHandlingTest {

    @Test
    void pautaNaoEncontradaException() {
        PautaNaoEncontradaException ex = new PautaNaoEncontradaException(1L);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void sessaoJaAbertaException() {
        SessaoJaAbertaException ex = new SessaoJaAbertaException(1L);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void sessaoNaoAbertaException() {
        SessaoNaoAbertaException ex = new SessaoNaoAbertaException(1L);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void sessaoEncerradaException() {
        SessaoEncerradaException ex = new SessaoEncerradaException(1L);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void votoDuplicadoException() {
        VotoDuplicadoException ex = new VotoDuplicadoException(1L, "12345678901");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void associadoNaoHabilitadoException() {
        AssociadoNaoHabilitadoException ex = new AssociadoNaoHabilitadoException("12345678901");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void cpfInvalidoException() {
        CpfInvalidoException ex = new CpfInvalidoException("12345678901");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void integracaoExternaIndisponivelException() {
        IntegracaoExternaIndisponivelException ex = new IntegracaoExternaIndisponivelException("12345678901", new Exception());
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
