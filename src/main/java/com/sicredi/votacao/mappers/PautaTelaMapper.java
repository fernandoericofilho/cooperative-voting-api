package com.sicredi.votacao.mappers;

import com.sicredi.votacao.dtos.Botao;
import com.sicredi.votacao.dtos.ItemFormulario;
import com.sicredi.votacao.dtos.ResultadoPauta;
import com.sicredi.votacao.dtos.TelaFormulario;
import com.sicredi.votacao.models.Pauta;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PautaTelaMapper {

    public TelaFormulario detalhe(Pauta pauta) {
        List<ItemFormulario> itens = List.of(
            ItemFormulario.texto("Título: " + pauta.getTitulo()),
            ItemFormulario.texto("Descrição: " + pauta.getDescricao()),
            ItemFormulario.texto("Status: " + status(pauta))
        );
        return new TelaFormulario("Detalhe da Pauta", itens, botaoDeAcao(pauta), null);
    }

    public TelaFormulario resultado(Pauta pauta, ResultadoPauta resultado) {
        List<ItemFormulario> itens = List.of(
            ItemFormulario.texto("Título: " + pauta.getTitulo()),
            ItemFormulario.texto("Sim: " + resultado.votosSim()),
            ItemFormulario.texto("Não: " + resultado.votosNao()),
            ItemFormulario.texto("Resultado: " + resultado.resultado())
        );
        return new TelaFormulario("Resultado da Votação", itens, new Botao("Voltar", "/api/v1/telas/home", Map.of()), null);
    }

    private String status(Pauta pauta) {
        if (!pauta.sessaoFoiAberta()) {
            return "Sessão não aberta";
        }
        return pauta.sessaoEstaAberta() ? "Sessão aberta" : "Sessão encerrada";
    }

    private Botao botaoDeAcao(Pauta pauta) {
        if (!pauta.sessaoFoiAberta()) {
            return new Botao("Abrir Sessão", "/api/v1/pautas/" + pauta.getId() + "/sessoes/tela", Map.of());
        }
        if (pauta.sessaoEstaAberta()) {
            return new Botao("Votar", "/api/v1/pautas/" + pauta.getId() + "/votos/tela", Map.of());
        }
        return new Botao("Ver Resultado", "/api/v1/pautas/" + pauta.getId() + "/resultado", Map.of());
    }
}
