package com.sicredi.voting.exceptions;

public class DuplicateVoteException extends RuntimeException {

    public DuplicateVoteException(Long agendaId, String cpf) {
        super("Member " + cpf + " already voted on agenda " + agendaId);
    }
}
