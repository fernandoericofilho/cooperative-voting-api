package com.sicredi.voting.exceptions;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExceptionHandlingTest {

    @Test
    void agendaNotFoundExceptionTest() {
        AgendaNotFoundException ex = new AgendaNotFoundException(1L);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void sessionAlreadyOpenExceptionTest() {
        SessionAlreadyOpenException ex = new SessionAlreadyOpenException(1L);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void sessionNotOpenExceptionTest() {
        SessionNotOpenException ex = new SessionNotOpenException(1L);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void sessionClosedExceptionTest() {
        SessionClosedException ex = new SessionClosedException(1L);
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void duplicateVoteExceptionTest() {
        DuplicateVoteException ex = new DuplicateVoteException(1L, "12345678901");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void memberNotEligibleExceptionTest() {
        MemberNotEligibleException ex = new MemberNotEligibleException("12345678901");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void invalidCpfExceptionTest() {
        InvalidCpfException ex = new InvalidCpfException("12345678901");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void externalIntegrationUnavailableExceptionTest() {
        ExternalIntegrationUnavailableException ex = new ExternalIntegrationUnavailableException("12345678901", new Exception());
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
