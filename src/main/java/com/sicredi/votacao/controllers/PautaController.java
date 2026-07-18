package com.sicredi.votacao.controllers;

import com.sicredi.votacao.controllers.request.CriarPautaRequest;
import com.sicredi.votacao.controllers.request.AbrirSessaoRequest;
import com.sicredi.votacao.dtos.PautaDTO;
import com.sicredi.votacao.dtos.ResultadoDTO;
import com.sicredi.votacao.mappers.DomainDTOMapper;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.services.PautaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pautas")
public class PautaController {

    private final PautaService pautaService;
    private final DomainDTOMapper mapper;

    public PautaController(PautaService pautaService, DomainDTOMapper mapper) {
        this.pautaService = pautaService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<PautaDTO> criarPauta(@Valid @RequestBody CriarPautaRequest request) {
        Pauta pauta = pautaService.criarPauta(request.titulo(), request.descricao());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toPautaDTO(pauta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PautaDTO> obterPauta(@PathVariable Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        return ResponseEntity.ok(mapper.toPautaDTO(pauta));
    }

    @PostMapping("/{id}/sessoes")
    public ResponseEntity<PautaDTO> abrirSessao(@PathVariable Long id, @Valid @RequestBody AbrirSessaoRequest request) {
        Pauta pauta = pautaService.abrirSessao(id, request.duracaoSegundos());
        return ResponseEntity.ok(mapper.toPautaDTO(pauta));
    }

    @GetMapping("/{id}/resultado")
    public ResponseEntity<ResultadoDTO> obterResultado(@PathVariable Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        var resultado = pautaService.apurarResultado(id);
        return ResponseEntity.ok(mapper.toResultadoDTO(pauta, resultado));
    }
}
