package com.sicredi.votacao.mappers;

import com.sicredi.votacao.controllers.response.PautaResponse;
import com.sicredi.votacao.controllers.response.ResultadoVotacaoResponse;
import com.sicredi.votacao.controllers.response.VotoResponse;
import com.sicredi.votacao.dtos.ResultadoPauta;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.models.Voto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DomainDTOMapper {

    public PautaResponse toPautaDTO(Pauta pauta) {
        String status = calculatePautaStatus(pauta);
        return new PautaResponse(
            pauta.getId(),
            pauta.getTitulo(),
            pauta.getDescricao(),
            pauta.getCriadaEm(),
            pauta.getSessaoAbertaEm(),
            pauta.getSessaoFechaEm(),
            status
        );
    }

    public VotoResponse toVotoDTO(Voto voto) {
        return new VotoResponse(
            voto.getId(),
            voto.getPautaId(),
            voto.getCpfAssociado(),
            voto.getVoto().name(),
            voto.getCriadoEm()
        );
    }

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
