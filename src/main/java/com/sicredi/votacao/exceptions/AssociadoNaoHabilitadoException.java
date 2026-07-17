package com.sicredi.votacao.exceptions;

public class AssociadoNaoHabilitadoException extends RuntimeException {

    public AssociadoNaoHabilitadoException(String cpf) {
        super("Associado não habilitado a votar: " + cpf);
    }
}
