package com.sicredi.voting.mappers;

import com.sicredi.voting.controllers.response.AgendaResponse;
import com.sicredi.voting.models.Agenda;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AgendaMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public AgendaResponse toAgendaDTO(Agenda agenda) {
        String status = calculateAgendaStatus(agenda);
        return new AgendaResponse(
            agenda.getId(),
            agenda.getTitle(),
            agenda.getDescription(),
            formatDateTime(agenda.getCreatedAt()),
            formatDateTime(agenda.getSessionOpenedAt()),
            formatDateTime(agenda.getSessionClosesAt()),
            status
        );
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(FORMATTER);
    }

    private String calculateAgendaStatus(Agenda agenda) {
        if (agenda.getSessionOpenedAt() == null) {
            return "NOT_STARTED";
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(agenda.getSessionClosesAt())) {
            return "OPEN";
        }
        return "CLOSED";
    }

}
