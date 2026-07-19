package com.sicredi.voting.controllers;

import com.sicredi.voting.controllers.request.CreateAgendaRequest;
import com.sicredi.voting.controllers.request.OpenSessionRequest;
import com.sicredi.voting.controllers.response.AgendaResponse;
import com.sicredi.voting.controllers.response.VotingResultResponse;
import com.sicredi.voting.mappers.AgendaMapper;
import com.sicredi.voting.mappers.VotingResultMapper;
import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.services.AgendaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agendas")
public class AgendaController {

    private final AgendaService agendaService;
    private final AgendaMapper agendaMapper;
    private final VotingResultMapper votingResultMapper;

    public AgendaController(AgendaService agendaService, AgendaMapper agendaMapper, VotingResultMapper votingResultMapper) {
        this.agendaService = agendaService;
        this.agendaMapper = agendaMapper;
        this.votingResultMapper = votingResultMapper;
    }

    @PostMapping
    public ResponseEntity<AgendaResponse> createAgenda(@Valid @RequestBody CreateAgendaRequest request) {
        Agenda agenda = agendaService.createAgenda(request.title(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(agendaMapper.toAgendaDTO(agenda));
    }

    @GetMapping
    public ResponseEntity<?> listAgendas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Agenda> agendasPage = agendaService.listAgendas(pageable);

        var agendasResponse = agendasPage.stream()
            .map(agendaMapper::toAgendaDTO)
            .toList();

        // Return clean Map without Spring Data fields
        var response = new java.util.LinkedHashMap<String, Object>();
        response.put("content", agendasResponse);
        response.put("totalElements", agendasPage.getTotalElements());
        response.put("totalPages", agendasPage.getTotalPages());
        response.put("currentPage", agendasPage.getNumber());
        response.put("pageSize", agendasPage.getSize());
        response.put("hasNext", agendasPage.hasNext());
        response.put("hasPrevious", agendasPage.hasPrevious());

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendaResponse> getAgenda(@PathVariable Long id) {
        Agenda agenda = agendaService.findById(id);
        return ResponseEntity.ok(agendaMapper.toAgendaDTO(agenda));
    }

    @PostMapping("/{id}/sessions")
    public ResponseEntity<AgendaResponse> openSession(@PathVariable Long id, @Valid @RequestBody OpenSessionRequest request) {
        Agenda agenda = agendaService.openSession(id, request.durationSeconds());
        return ResponseEntity.ok(agendaMapper.toAgendaDTO(agenda));
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<VotingResultResponse> getResult(@PathVariable Long id) {
        Agenda agenda = agendaService.findById(id);
        var result = agendaService.tallyResult(id);
        return ResponseEntity.ok(votingResultMapper.toResultDTO(agenda, result));
    }
}
