package com.sicredi.voting.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsAllDomainExceptions() {
        assertStatus(handler.handleAgendaNotFound(new AgendaNotFoundException(1L)), HttpStatus.NOT_FOUND);
        assertStatus(handler.handleSessionAlreadyOpen(new SessionAlreadyOpenException(1L)), HttpStatus.CONFLICT);
        assertStatus(handler.handleSessionNotOpen(new SessionNotOpenException(1L)), HttpStatus.UNPROCESSABLE_ENTITY);
        assertStatus(handler.handleSessionClosed(new SessionClosedException(1L)), HttpStatus.UNPROCESSABLE_ENTITY);
        assertStatus(handler.handleDuplicateVote(new DuplicateVoteException(1L, "111")), HttpStatus.CONFLICT);
        assertStatus(handler.handleMemberNotEligible(new MemberNotEligibleException("111")), HttpStatus.UNPROCESSABLE_ENTITY);
        assertStatus(handler.handleInvalidCpf(new InvalidCpfException("111")), HttpStatus.BAD_REQUEST);
        assertStatus(handler.handleExternalIntegrationUnavailable(new ExternalIntegrationUnavailableException("111", new RuntimeException())), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void handleGeneralException() {
        var exception = new RuntimeException("Unexpected error in application");
        var response = handler.handleGeneralException(exception);

        assertStatus(response, HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Internal server error");
    }

    private void assertStatus(ResponseEntity<ErrorResponse> response, HttpStatus expected) {
        assertThat(response.getStatusCode()).isEqualTo(expected);
        assertThat(response.getBody()).isNotNull();
    }
}
