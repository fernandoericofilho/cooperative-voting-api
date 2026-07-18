package com.sicredi.votacao.mappers;

import com.sicredi.votacao.controllers.response.PautaResponse;
import com.sicredi.votacao.models.Pauta;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PautaMapper {

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
