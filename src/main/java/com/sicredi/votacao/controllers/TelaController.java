package com.sicredi.votacao.controllers;

import com.sicredi.votacao.dtos.Botao;
import com.sicredi.votacao.dtos.ItemFormulario;
import com.sicredi.votacao.dtos.ItemSelecao;
import com.sicredi.votacao.dtos.TelaFormulario;
import com.sicredi.votacao.dtos.TelaSelecao;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.services.PautaService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/telas")
public class TelaController {

    private final PautaService pautaService;

    public TelaController(PautaService pautaService) {
        this.pautaService = pautaService;
    }

    @GetMapping("/home")
    public TelaSelecao home() {
        List<ItemSelecao> itens = List.of(
            new ItemSelecao("Cadastrar Pauta", "/api/v1/telas/pautas/novo"),
            new ItemSelecao("Listar Pautas", "/api/v1/telas/pautas")
        );
        return new TelaSelecao("Menu", itens);
    }

    @PostMapping("/pautas/novo")
    public TelaFormulario novaPauta() {
        List<ItemFormulario> itens = List.of(
            ItemFormulario.inputTexto("titulo", "Título", ""),
            ItemFormulario.inputTexto("descricao", "Descrição", "")
        );
        return new TelaFormulario("Nova Pauta", itens, new Botao("Cadastrar", "/api/v1/pautas", Map.of()), null);
    }

    @PostMapping("/pautas")
    public TelaSelecao listarPautas() {
        List<ItemSelecao> itens = pautaService.listarTodas().stream()
            .map(this::paraItem)
            .collect(Collectors.toList());
        return new TelaSelecao("Lista de Pautas", itens);
    }

    private ItemSelecao paraItem(Pauta pauta) {
        return new ItemSelecao(pauta.getTitulo(), "/api/v1/pautas/" + pauta.getId());
    }
}
