package com.sicredi.votacao.controllers;

import com.sicredi.votacao.controllers.request.AbrirSessaoRequest;
import com.sicredi.votacao.controllers.request.CriarPautaRequest;
import com.sicredi.votacao.dtos.ResultadoPauta;
import com.sicredi.votacao.dtos.TelaFormulario;
import com.sicredi.votacao.mappers.PautaTelaMapper;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.services.PautaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas")
public class PautaController {

    private final PautaService pautaService;
    private final PautaTelaMapper pautaTelaMapper;

    public PautaController(PautaService pautaService, PautaTelaMapper pautaTelaMapper) {
        this.pautaService = pautaService;
        this.pautaTelaMapper = pautaTelaMapper;
    }

    @PostMapping
    public TelaFormulario criar(@Valid @RequestBody CriarPautaRequest request) {
        Pauta pauta = pautaService.criarPauta(request.titulo(), request.descricao());
        return pautaTelaMapper.detalhe(pauta);
    }

    @PostMapping("/{id}")
    public TelaFormulario detalhe(@PathVariable Long id) {
        return pautaTelaMapper.detalhe(pautaService.buscarPorId(id));
    }

    @PostMapping("/{id}/sessoes")
    public TelaFormulario abrirSessao(@PathVariable Long id, @RequestBody(required = false) AbrirSessaoRequest request) {
        Long duracao = request != null ? request.duracaoSegundos() : null;
        Pauta pauta = pautaService.abrirSessao(id, duracao);
        return pautaTelaMapper.detalhe(pauta);
    }

    @PostMapping("/{id}/resultado")
    public TelaFormulario resultado(@PathVariable Long id) {
        Pauta pauta = pautaService.buscarPorId(id);
        ResultadoPauta resultado = pautaService.apurarResultado(id);
        return pautaTelaMapper.resultado(pauta, resultado);
    }
}
