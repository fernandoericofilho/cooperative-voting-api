package com.sicredi.voting.controllers.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAgendaRequest(@NotBlank String title, String description) {
}
