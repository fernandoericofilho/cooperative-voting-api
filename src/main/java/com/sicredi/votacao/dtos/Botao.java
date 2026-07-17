package com.sicredi.votacao.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Botao(String texto, String url, Map<String, Object> body) {
}
