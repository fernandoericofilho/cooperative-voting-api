package com.sicredi.votacao.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

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

    @Test
    void tratarValidacaoFalhada() {
        var errors = new ArrayList<ObjectError>();
        errors.add(new FieldError("request", "campo1", "não pode ser nulo"));
        errors.add(new FieldError("request", "campo2", "deve estar em formato válido"));

        var bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(errors);

        var methodParameter = mock(MethodParameter.class);
        var exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        var response = handler.handleMethodArgumentNotValid(exception);

        assertStatus(response, HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("Validação falhou");
        assertThat(response.getBody().message()).contains("campo1");
        assertThat(response.getBody().message()).contains("campo2");
    }

    @Test
    void tratarExcecaoGeneral() {
        var exception = new RuntimeException("Erro inesperado na aplicação");
        var response = handler.handleGeneralException(exception);

        assertStatus(response, HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Erro interno do servidor");
    }

    private void assertStatus(ResponseEntity<ErrorResponse> response, HttpStatus expected) {
        assertThat(response.getStatusCode()).isEqualTo(expected);
        assertThat(response.getBody()).isNotNull();
    }
}
