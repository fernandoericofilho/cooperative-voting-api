package com.sicredi.votacao.mappers;

import com.sicredi.votacao.dtos.PautaDTO;
import com.sicredi.votacao.dtos.ResultadoDTO;
import com.sicredi.votacao.dtos.ResultadoPauta;
import com.sicredi.votacao.dtos.VotoDTO;
import com.sicredi.votacao.models.Pauta;
import com.sicredi.votacao.models.Voto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DomainDTOMapper {

    public PautaDTO toPautaDTO(Pauta pauta) {
        String status = calculatePautaStatus(pauta);
        return new PautaDTO(
            pauta.getId(),
            pauta.getTitulo(),
            pauta.getDescricao(),
            pauta.getCriadaEm(),
            pauta.getSessaoAbertaEm(),
            pauta.getSessaoFechaEm(),
            status
        );
    }

    public VotoDTO toVotoDTO(Voto voto) {
        return new VotoDTO(
            voto.getId(),
            voto.getPautaId(),
            voto.getCpfAssociado(),
            voto.getVoto().name(),
            voto.getCriadoEm()
        );
    }

    public ResultadoDTO toResultadoDTO(Pauta pauta, ResultadoPauta resultadoPauta) {
        return new ResultadoDTO(
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
