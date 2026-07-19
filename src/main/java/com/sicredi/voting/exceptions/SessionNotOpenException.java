package com.sicredi.voting.exceptions;

public class SessionNotOpenException extends RuntimeException {

    public SessionNotOpenException(Long agendaId) {
        super("Voting session not open for agenda: " + agendaId);
    }
}
