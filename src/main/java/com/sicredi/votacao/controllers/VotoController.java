package com.sicredi.votacao.controllers;

import com.sicredi.votacao.controllers.request.RegistrarVotoRequest;
import com.sicredi.votacao.controllers.response.VotoResponse;
import com.sicredi.votacao.mappers.DomainDTOMapper;
import com.sicredi.votacao.models.OpcaoVoto;
import com.sicredi.votacao.models.Voto;
import com.sicredi.votacao.services.VotoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/votos")
public class VotoController {

    private final VotoService votoService;
    private final DomainDTOMapper mapper;

    public VotoController(VotoService votoService, DomainDTOMapper mapper) {
        this.votoService = votoService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<VotoResponse> registrarVoto(
            @PathVariable Long pautaId,
            @Valid @RequestBody RegistrarVotoRequest request) {
        Voto voto = votoService.registrarVoto(pautaId, request.cpfAssociado(), OpcaoVoto.valueOf(request.voto()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toVotoDTO(voto));
    }
}
