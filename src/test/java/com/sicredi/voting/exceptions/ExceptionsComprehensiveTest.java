package com.sicredi.voting.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Exceptions Comprehensive Coverage Tests")
class ExceptionsComprehensiveTest {

    @Test
    @DisplayName("ExternalIntegrationUnavailableException with two args")
    void testExternalIntegrationUnavailableExceptionTwoArgs() {
        Exception cause = new RuntimeException("Network error");
        ExternalIntegrationUnavailableException ex =
            new ExternalIntegrationUnavailableException("12345678901", cause);

        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("12345678901") || ex.getMessage().contains("unavailable"));
    }

    @Test
    @DisplayName("ExternalIntegrationUnavailableException with message and cause")
    void testExternalIntegrationUnavailableExceptionMessageAndCause() {
        Exception cause = new RuntimeException("Service down");
        ExternalIntegrationUnavailableException ex =
            new ExternalIntegrationUnavailableException("Service error", cause);

        assertNotNull(ex);
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("InvalidCpfException instantiation")
    void testInvalidCpfException() {
        InvalidCpfException ex = new InvalidCpfException("invalid");
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("invalid"));
    }

    @Test
    @DisplayName("AgendaNotFoundException instantiation")
    void testAgendaNotFoundException() {
        AgendaNotFoundException ex = new AgendaNotFoundException(999L);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    @DisplayName("SessionAlreadyOpenException instantiation")
    void testSessionAlreadyOpenException() {
        SessionAlreadyOpenException ex = new SessionAlreadyOpenException(123L);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("123"));
    }

    @Test
    @DisplayName("SessionNotOpenException instantiation")
    void testSessionNotOpenException() {
        SessionNotOpenException ex = new SessionNotOpenException(456L);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("456"));
    }

    @Test
    @DisplayName("SessionClosedException instantiation")
    void testSessionClosedException() {
        SessionClosedException ex = new SessionClosedException(789L);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("789"));
    }

    @Test
    @DisplayName("DuplicateVoteException instantiation")
    void testDuplicateVoteException() {
        DuplicateVoteException ex = new DuplicateVoteException(111L, "12345678901");
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("12345678901"));
    }

    @Test
    @DisplayName("MemberNotEligibleException instantiation")
    void testMemberNotEligibleException() {
        MemberNotEligibleException ex = new MemberNotEligibleException("98765432100");
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("98765432100"));
    }
}
