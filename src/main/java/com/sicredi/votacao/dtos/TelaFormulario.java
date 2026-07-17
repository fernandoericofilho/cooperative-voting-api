package com.sicredi.votacao.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TelaFormulario(String tipo, String titulo, List<ItemFormulario> itens, Botao botaoOk, Botao botaoCancelar) {

    public TelaFormulario(String titulo, List<ItemFormulario> itens, Botao botaoOk, Botao botaoCancelar) {
        this("FORMULARIO", titulo, itens, botaoOk, botaoCancelar);
    }
}
