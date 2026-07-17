package com.sicredi.votacao.controllers.request;

import jakarta.validation.constraints.NotBlank;

public record CriarPautaRequest(@NotBlank String titulo, String descricao) {
}
