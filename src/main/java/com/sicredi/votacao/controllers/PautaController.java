package com.sicredi.votacao.controllers;

import com.sicredi.votacao.controllers.request.CriarPautaRequest;
import com.sicredi.votacao.controllers.request.AbrirSessaoRequest;
import com.sicredi.votacao.controllers.response.PautaResponse;
import com.sicredi.votacao.controllers.response.ResultadoVotacaoResponse;
import com.sicredi.votacao.mappers.PautaDtoMapper;
import com.sicredi.votacao.mappers.ResultadoVotacaoDtoMapper;
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
    private final PautaDtoMapper pautaDtoMapper;
    private final ResultadoVotacaoDtoMapper resultadoVotacaoDtoMapper;

    public PautaController(PautaService pautaService, PautaDtoMapper pautaDtoMapper, ResultadoVotacaoDtoMapper resultadoVotacaoDtoMapper) {
        this.pautaService = pautaService;
        this.pautaDtoMapper = pautaDtoMapper;
        this.resultadoVotacaoDtoMapper = resultadoVotacaoDtoMapper;
    }

    @PostMapping
    public ResponseEntity<PautaResponse> criarPauta(@Valid @RequestBody CriarPautaRequest request) {
        Pauta pauta = pautaService.criarPauta(request.titulo(), request.descricao());
        return ResponseEntity.status(HttpStatus.CREATED).body(pautaDtoMapper.toPautaDTO(pauta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PautaResponse> obterPauta(@PathVariable Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        return ResponseEntity.ok(pautaDtoMapper.toPautaDTO(pauta));
    }

    @PostMapping("/{id}/sessoes")
    public ResponseEntity<PautaResponse> abrirSessao(@PathVariable Long id, @Valid @RequestBody AbrirSessaoRequest request) {
        Pauta pauta = pautaService.abrirSessao(id, request.duracaoSegundos());
        return ResponseEntity.ok(pautaDtoMapper.toPautaDTO(pauta));
    }

    @GetMapping("/{id}/resultado")
    public ResponseEntity<ResultadoVotacaoResponse> obterResultado(@PathVariable Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        var resultado = pautaService.apurarResultado(id);
        return ResponseEntity.ok(resultadoVotacaoDtoMapper.toResultadoDTO(pauta, resultado));
    }
}
