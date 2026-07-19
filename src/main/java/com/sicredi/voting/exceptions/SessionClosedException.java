package com.sicredi.voting.exceptions;

public class SessionClosedException extends RuntimeException {

    public SessionClosedException(Long agendaId) {
        super("Voting session already closed for agenda: " + agendaId);
    }
}
