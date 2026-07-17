package com.sicredi.votacao.exceptions;

public class IntegracaoExternaIndisponivelException extends RuntimeException {

    public IntegracaoExternaIndisponivelException(String cpf, Throwable cause) {
        super("Não foi possível validar o associado " + cpf + " no momento", cause);
    }
}
