package com.sicredi.votacao.controllers;

import com.sicredi.votacao.controllers.request.CpfFormularioRequest;
import com.sicredi.votacao.dtos.Botao;
import com.sicredi.votacao.dtos.ItemFormulario;
import com.sicredi.votacao.dtos.ItemSelecao;
import com.sicredi.votacao.dtos.TelaFormulario;
import com.sicredi.votacao.dtos.TelaSelecao;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/votos")
public class VotoTelaController {

    @PostMapping("/tela")
    public TelaFormulario telaVoto(@PathVariable Long pautaId) {
        List<ItemFormulario> itens = List.of(ItemFormulario.inputTexto("cpfAssociado", "CPF do associado", ""));
        Botao botaoOk = new Botao("Continuar", "/api/v1/pautas/" + pautaId + "/votos/opcoes", Map.of());
        return new TelaFormulario("Votar", itens, botaoOk, null);
    }

    @PostMapping("/opcoes")
    public TelaSelecao opcoes(@PathVariable Long pautaId, @RequestBody CpfFormularioRequest request) {
        String url = "/api/v1/pautas/" + pautaId + "/votos";
        List<ItemSelecao> itens = List.of(
            new ItemSelecao("Sim", url, Map.of("cpfAssociado", request.cpfAssociado(), "voto", "SIM")),
            new ItemSelecao("Não", url, Map.of("cpfAssociado", request.cpfAssociado(), "voto", "NAO"))
        );
        return new TelaSelecao("Confirme seu voto", itens);
    }
}
