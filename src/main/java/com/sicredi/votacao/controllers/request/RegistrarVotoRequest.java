package com.sicredi.votacao.controllers.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegistrarVotoRequest(
    @NotBlank @Pattern(regexp = "\\d{11}") String cpfAssociado,
    @NotBlank @Pattern(regexp = "SIM|NAO") String voto
) {
}
