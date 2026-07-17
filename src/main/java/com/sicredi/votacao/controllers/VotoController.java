package com.sicredi.votacao.controllers;

import com.sicredi.votacao.controllers.request.RegistrarVotoRequest;
import com.sicredi.votacao.dtos.Botao;
import com.sicredi.votacao.dtos.ItemFormulario;
import com.sicredi.votacao.dtos.TelaFormulario;
import com.sicredi.votacao.models.OpcaoVoto;
import com.sicredi.votacao.services.VotoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/votos")
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @PostMapping
    public TelaFormulario registrar(@PathVariable Long pautaId, @Valid @RequestBody RegistrarVotoRequest request) {
        votoService.registrarVoto(pautaId, request.cpfAssociado(), OpcaoVoto.valueOf(request.voto()));
        List<ItemFormulario> itens = List.of(ItemFormulario.texto("Voto registrado com sucesso"));
        return new TelaFormulario("Confirmação", itens, new Botao("Voltar", "/api/v1/telas/home", Map.of()), null);
    }
}
