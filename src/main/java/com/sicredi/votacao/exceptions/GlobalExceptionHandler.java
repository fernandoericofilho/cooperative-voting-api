package com.sicredi.votacao.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PautaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handlePautaNaoEncontrada(PautaNaoEncontradaException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(SessaoJaAbertaException.class)
    public ResponseEntity<ErrorResponse> handleSessaoJaAberta(SessaoJaAbertaException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(SessaoNaoAbertaException.class)
    public ResponseEntity<ErrorResponse> handleSessaoNaoAberta(SessaoNaoAbertaException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(SessaoEncerradaException.class)
    public ResponseEntity<ErrorResponse> handleSessaoEncerrada(SessaoEncerradaException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(VotoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleVotoDuplicado(VotoDuplicadoException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AssociadoNaoHabilitadoException.class)
    public ResponseEntity<ErrorResponse> handleAssociadoNaoHabilitado(AssociadoNaoHabilitadoException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(CpfInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleCpfInvalido(CpfInvalidoException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IntegracaoExternaIndisponivelException.class)
    public ResponseEntity<ErrorResponse> handleIntegracaoIndisponivel(IntegracaoExternaIndisponivelException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
            .body(ErrorResponse.of(status.value(), status.getReasonPhrase(), message));
    }
}
