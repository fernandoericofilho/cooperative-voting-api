package com.sicredi.votacao.dtos;

public record ResultadoPauta(long votosSim, long votosNao, String resultado) {

    public static ResultadoPauta calcular(long votosSim, long votosNao) {
        String resultado;
        if (votosSim > votosNao) {
            resultado = "APROVADA";
        } else if (votosNao > votosSim) {
            resultado = "REPROVADA";
        } else {
            resultado = "EMPATE";
        }
        return new ResultadoPauta(votosSim, votosNao, resultado);
    }
}
