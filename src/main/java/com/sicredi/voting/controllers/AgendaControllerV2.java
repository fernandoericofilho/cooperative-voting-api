package com.sicredi.voting.controllers;

import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.services.AgendaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v2/agendas")
public class AgendaControllerV2 {

    private final AgendaService agendaService;

    public AgendaControllerV2(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @GetMapping
    public List<SimpleAgendaDTO> listAgendas() {
        return agendaService.listAll()
            .stream()
            .map(SimpleAgendaDTO::fromAgenda)
            .toList();
    }

    record SimpleAgendaDTO(
        Long id,
        String title,
        String status,
        LocalDateTime createdAt
    ) {
        static SimpleAgendaDTO fromAgenda(Agenda agenda) {
            return new SimpleAgendaDTO(
                agenda.getId(),
                agenda.getTitle(),
                agenda.getStatus(),
                agenda.getCreatedAt()
            );
        }
    }
}
