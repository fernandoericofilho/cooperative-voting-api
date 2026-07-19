package com.sicredi.voting.exceptions;

public class SessionAlreadyOpenException extends RuntimeException {

    public SessionAlreadyOpenException(Long agendaId) {
        super("Session already open for agenda: " + agendaId);
    }
}
