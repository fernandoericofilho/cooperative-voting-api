package com.sicredi.votacao.exceptions;

public class VotoDuplicadoException extends RuntimeException {

    public VotoDuplicadoException(Long pautaId, String cpf) {
        super("Associado " + cpf + " já votou na pauta " + pautaId);
    }
}
