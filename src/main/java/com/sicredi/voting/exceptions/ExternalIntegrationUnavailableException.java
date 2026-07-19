package com.sicredi.voting.exceptions;

public class ExternalIntegrationUnavailableException extends RuntimeException {

    public ExternalIntegrationUnavailableException(String cpf, Throwable cause) {
        super("Could not validate member " + cpf + " at this moment", cause);
    }
}
