package com.sicredi.votacao.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapeiaTodasAsExcecoesDeDominio() {
        assertStatus(handler.handlePautaNaoEncontrada(new PautaNaoEncontradaException(1L)), HttpStatus.NOT_FOUND);
        assertStatus(handler.handleSessaoJaAberta(new SessaoJaAbertaException(1L)), HttpStatus.CONFLICT);
        assertStatus(handler.handleSessaoNaoAberta(new SessaoNaoAbertaException(1L)), HttpStatus.UNPROCESSABLE_ENTITY);
        assertStatus(handler.handleSessaoEncerrada(new SessaoEncerradaException(1L)), HttpStatus.UNPROCESSABLE_ENTITY);
        assertStatus(handler.handleVotoDuplicado(new VotoDuplicadoException(1L, "111")), HttpStatus.CONFLICT);
        assertStatus(handler.handleAssociadoNaoHabilitado(new AssociadoNaoHabilitadoException("111")), HttpStatus.UNPROCESSABLE_ENTITY);
        assertStatus(handler.handleCpfInvalido(new CpfInvalidoException("111")), HttpStatus.BAD_REQUEST);
        assertStatus(handler.handleIntegracaoIndisponivel(new IntegracaoExternaIndisponivelException("111", new RuntimeException())), HttpStatus.SERVICE_UNAVAILABLE);
    }

    private void assertStatus(ResponseEntity<ErrorResponse> response, HttpStatus expected) {
        assertThat(response.getStatusCode()).isEqualTo(expected);
        assertThat(response.getBody()).isNotNull();
    }
}
