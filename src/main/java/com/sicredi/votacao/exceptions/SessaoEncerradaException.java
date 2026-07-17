package com.sicredi.votacao.exceptions;

public class SessaoEncerradaException extends RuntimeException {

    public SessaoEncerradaException(Long pautaId) {
        super("Sessão de votação já encerrada para a pauta: " + pautaId);
    }
}
