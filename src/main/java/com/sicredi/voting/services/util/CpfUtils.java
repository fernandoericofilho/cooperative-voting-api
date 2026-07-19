package com.sicredi.voting.services.util;

public class CpfUtils {

    private CpfUtils() {
    }

    public static String mask(String cpf) {
        if (cpf == null || cpf.length() < 4) {
            return "****";
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(cpf.length() - 2);
    }
}
