package com.sicredi.voting.mappers;

import com.sicredi.voting.controllers.response.VotingResultResponse;
import com.sicredi.voting.dtos.AgendaResultDto;
import com.sicredi.voting.models.Agenda;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class VotingResultMapper {

    public VotingResultResponse toResultDTO(Agenda agenda, AgendaResultDto resultAgenda) {
        return new VotingResultResponse(
            agenda.getId(),
            agenda.getTitle(),
            resultAgenda.yesCount(),
            resultAgenda.noCount(),
            resultAgenda.result()
        );
    }

}
