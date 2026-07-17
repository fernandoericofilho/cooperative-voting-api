package com.sicredi.votacao.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemFormulario(String tipo, String id, String titulo, String texto, Object valor) {

    public static ItemFormulario texto(String texto) {
        return new ItemFormulario("TEXTO", null, null, texto, null);
    }

    public static ItemFormulario inputTexto(String id, String titulo, Object valor) {
        return new ItemFormulario("INPUT_TEXTO", id, titulo, null, valor);
    }

    public static ItemFormulario inputNumero(String id, String titulo, Object valor) {
        return new ItemFormulario("INPUT_NUMERO", id, titulo, null, valor);
    }
}
