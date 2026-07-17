package com.sicredi.votacao.exceptions;

public class SessaoJaAbertaException extends RuntimeException {

    public SessaoJaAbertaException(Long pautaId) {
        super("Sessão já aberta para a pauta: " + pautaId);
    }
}
