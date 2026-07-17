package com.sicredi.votacao.exceptions;

public class PautaNaoEncontradaException extends RuntimeException {

    public PautaNaoEncontradaException(Long id) {
        super("Pauta não encontrada: " + id);
    }
}
