package com.sicredi.votacao.mappers;

import com.sicredi.votacao.controllers.response.PautaResponse;
import com.sicredi.votacao.models.Pauta;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class PautaMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public PautaResponse toPautaDTO(Pauta pauta) {
        String status = calculatePautaStatus(pauta);
        return new PautaResponse(
            pauta.getId(),
            pauta.getTitulo(),
            pauta.getDescricao(),
            formatDateTime(pauta.getCriadaEm()),
            formatDateTime(pauta.getSessaoAbertaEm()),
            formatDateTime(pauta.getSessaoFechaEm()),
            status
        );
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(FORMATTER);
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
