package com.sicredi.votacao.exceptions;

public class SessaoNaoAbertaException extends RuntimeException {

    public SessaoNaoAbertaException(Long pautaId) {
        super("Sessão de votação ainda não foi aberta para a pauta: " + pautaId);
    }
}
