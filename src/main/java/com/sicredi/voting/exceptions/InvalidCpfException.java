package com.sicredi.voting.exceptions;

public class InvalidCpfException extends RuntimeException {

    public InvalidCpfException(String cpf) {
        super("Invalid CPF: " + cpf);
    }
}
