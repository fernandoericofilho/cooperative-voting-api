package com.sicredi.votacao.mappers;

import com.sicredi.votacao.controllers.response.VotoResponse;
import com.sicredi.votacao.models.Voto;
import org.springframework.stereotype.Component;

@Component
public class VotoDtoMapper {

    public VotoResponse toVotoDTO(Voto voto) {
        return new VotoResponse(
            voto.getId(),
            voto.getPautaId(),
            voto.getCpfAssociado(),
            voto.getVoto().name(),
            voto.getCriadoEm()
        );
    }

}
