package com.sicredi.voting.exceptions;

public class AgendaNotFoundException extends RuntimeException {

    public AgendaNotFoundException(Long id) {
        super("Agenda not found: " + id);
    }
}
