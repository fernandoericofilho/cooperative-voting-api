package com.sicredi.voting.exceptions;

public class MemberNotEligibleException extends RuntimeException {

    public MemberNotEligibleException(String cpf) {
        super("Member not eligible to vote: " + cpf);
    }
}
