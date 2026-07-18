package com.sicredi.votacao.mappers;

import com.sicredi.votacao.controllers.response.ResultadoVotacaoResponse;
import com.sicredi.votacao.dtos.ResultadoPauta;
import com.sicredi.votacao.models.Pauta;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ResultadoVotacaoDtoMapper {

    public ResultadoVotacaoResponse toResultadoDTO(Pauta pauta, ResultadoPauta resultadoPauta) {
        return new ResultadoVotacaoResponse(
            pauta.getId(),
            (int) resultadoPauta.votosSim(),
            (int) resultadoPauta.votosNao(),
            resultadoPauta.resultado(),
            calculatePautaStatus(pauta)
        );
    }

    private String calculatePautaStatus(Pauta pauta) {
        if (pauta.getSessaoAbertaEm() == null) {
            return "NAO_INICIADA";
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(pauta.getSessaoFechaEm())) {
            return "ABERTA";
        }
        return "ENCERRADA";
    }

}
