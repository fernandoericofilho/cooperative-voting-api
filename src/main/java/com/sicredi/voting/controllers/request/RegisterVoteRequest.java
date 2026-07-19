package com.sicredi.voting.controllers.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterVoteRequest(
    @NotBlank @Pattern(regexp = "\\d{11}") String memberCpf,
    @NotBlank @Pattern(regexp = "YES|NO") String vote
) {
}
