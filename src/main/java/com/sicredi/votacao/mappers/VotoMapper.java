package com.sicredi.votacao.mappers;

import com.sicredi.votacao.controllers.response.VotoResponse;
import com.sicredi.votacao.models.Voto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class VotoMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public VotoResponse toVotoDTO(Voto voto) {
        return new VotoResponse(
            voto.getId(),
            voto.getPautaId(),
            voto.getCpfAssociado(),
            voto.getVoto().name(),
            formatDateTime(voto.getCriadoEm())
        );
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(FORMATTER);
    }

}
