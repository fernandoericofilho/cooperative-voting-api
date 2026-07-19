package com.sicredi.votacao.controllers;

import com.sicredi.votacao.controllers.request.CriarPautaRequest;
import com.sicredi.votacao.controllers.request.AbrirSessaoRequest;
import com.sicredi.votacao.controllers.response.PautaResponse;
import com.sicredi.votacao.controllers.response.PautasListResponse;
import com.sicredi.votacao.controllers.response.ResultadoVotacaoResponse;
import com.sicredi.votacao.mappers.PautaMapper;
import com.sicredi.votacao.mappers.ResultadoVotacaoMapper;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.services.PautaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pautas")
public class PautaController {

    private final PautaService pautaService;
    private final PautaMapper pautaMapper;
    private final ResultadoVotacaoMapper resultadoVotacaoMapper;

    public PautaController(PautaService pautaService, PautaMapper pautaMapper, ResultadoVotacaoMapper resultadoVotacaoMapper) {
        this.pautaService = pautaService;
        this.pautaMapper = pautaMapper;
        this.resultadoVotacaoMapper = resultadoVotacaoMapper;
    }

    @PostMapping
    public ResponseEntity<PautaResponse> criarPauta(@Valid @RequestBody CriarPautaRequest request) {
        Pauta pauta = pautaService.criarPauta(request.titulo(), request.descricao());
        return ResponseEntity.status(HttpStatus.CREATED).body(pautaMapper.toPautaDTO(pauta));
    }

    @GetMapping
    public ResponseEntity<PautasListResponse> listarPautas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Pauta> pautasPage = pautaService.listarPautas(pageable);

        var pautasResponse = pautasPage.stream()
            .map(pautaMapper::toPautaDTO)
            .toList();

        PautasListResponse response = new PautasListResponse(
            pautasResponse,
            pautasPage.getTotalElements(),
            pautasPage.getTotalPages(),
            pautasPage.getNumber(),
            pautasPage.getSize(),
            pautasPage.hasNext(),
            pautasPage.hasPrevious()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PautaResponse> obterPauta(@PathVariable Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        return ResponseEntity.ok(pautaMapper.toPautaDTO(pauta));
    }

    @PostMapping("/{id}/sessoes")
    public ResponseEntity<PautaResponse> abrirSessao(@PathVariable Long id, @Valid @RequestBody AbrirSessaoRequest request) {
        Pauta pauta = pautaService.abrirSessao(id, request.duracaoSegundos());
        return ResponseEntity.ok(pautaMapper.toPautaDTO(pauta));
    }

    @GetMapping("/{id}/resultado")
    public ResponseEntity<ResultadoVotacaoResponse> obterResultado(@PathVariable Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        var resultado = pautaService.apurarResultado(id);
        return ResponseEntity.ok(resultadoVotacaoMapper.toResultadoDTO(pauta, resultado));
    }
}
