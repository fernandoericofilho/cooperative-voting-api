package com.sicredi.voting.mappers;

import com.sicredi.voting.controllers.response.VoteResponse;
import com.sicredi.voting.models.Vote;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class VoteMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public VoteResponse toVoteDTO(Vote vote) {
        return new VoteResponse(
            vote.getId(),
            vote.getAgendaId(),
            vote.getMemberCpf(),
            vote.getVote().name(),
            formatDateTime(vote.getCreatedAt())
        );
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(FORMATTER);
    }

}
