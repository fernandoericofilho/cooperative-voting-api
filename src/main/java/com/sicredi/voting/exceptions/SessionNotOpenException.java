package com.sicredi.voting.exceptions;

public class SessionNotOpenException extends RuntimeException {

    public SessionNotOpenException(Long agendaId) {
        super("Voting session not yet opened for agenda: " + agendaId);
    }
}
