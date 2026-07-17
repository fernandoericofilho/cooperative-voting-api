package com.sicredi.votacao.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TelaSelecao(String tipo, String titulo, List<ItemSelecao> itens) {

    public TelaSelecao(String titulo, List<ItemSelecao> itens) {
        this("SELECAO", titulo, itens);
    }
}
