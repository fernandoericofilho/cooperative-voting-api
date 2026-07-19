package com.sicredi.voting.mappers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.voting.controllers.response.VotingResultResponse;
import com.sicredi.voting.dtos.AgendaResultDto;
import com.sicredi.voting.models.Agenda;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class VotingResultMapperCoverageTest {

    private final VotingResultMapper mapper = new VotingResultMapper();

    @Test
    void mapResultVotingAgendaNotStarted() {
        Agenda agenda = new Agenda("Reform", "Description");
        AgendaResultDto result = new AgendaResultDto(10L, 5L, "APPROVED");

        VotingResultResponse response = mapper.toResultDTO(agenda, result);

        assertThat(response.getAgendaId()).isEqualTo(agenda.getId());
        assertThat(response.getYesCount()).isEqualTo(10);
        assertThat(response.getNoCount()).isEqualTo(5);
        assertThat(response.getResult()).isEqualTo("APPROVED");
    }

    @Test
    void mapResultVotingAgendaOpen() {
        Agenda agenda = new Agenda("Reform", "Description");
        agenda.openSession(600L);

        AgendaResultDto result = new AgendaResultDto(8L, 4L, "APPROVED");

        VotingResultResponse response = mapper.toResultDTO(agenda, result);

        assertThat(response.getYesCount()).isEqualTo(8);
        assertThat(response.getNoCount()).isEqualTo(4);
    }

    @Test
    void mapResultVotingAgendaClosed() {
        Agenda agenda = new Agenda("Reform", "Description");
        agenda.openSession(1L);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        AgendaResultDto result = new AgendaResultDto(6L, 3L, "APPROVED");

        VotingResultResponse response = mapper.toResultDTO(agenda, result);

        assertThat(response.getYesCount()).isEqualTo(6);
        assertThat(response.getNoCount()).isEqualTo(3);
    }

    @Test
    void mapResultWithRejection() {
        Agenda agenda = new Agenda("Fundamental Law", "Desc");
        agenda.setId(3L);
        agenda.openSession(60L);

        AgendaResultDto result = new AgendaResultDto(15L, 20L, "REJECTED");

        VotingResultResponse response = mapper.toResultDTO(agenda, result);

        assertThat(response.getAgendaId()).isEqualTo(3L);
        assertThat(response.getYesCount()).isEqualTo(15);
        assertThat(response.getNoCount()).isEqualTo(20);
        assertThat(response.getResult()).isEqualTo("REJECTED");
    }

    @Test
    void mapResultTie() {
        Agenda agenda = new Agenda("Voting", "Tie");
        agenda.openSession(60L);

        AgendaResultDto result = new AgendaResultDto(5L, 5L, "TIE");

        VotingResultResponse response = mapper.toResultDTO(agenda, result);

        assertThat(response.getYesCount()).isEqualTo(5);
        assertThat(response.getNoCount()).isEqualTo(5);
    }

    @Test
    void mapResultWithZeroVotes() {
        Agenda agenda = new Agenda("Voting", "No votes");
        agenda.openSession(60L);

        AgendaResultDto result = new AgendaResultDto(0L, 0L, "TIE");

        VotingResultResponse response = mapper.toResultDTO(agenda, result);

        assertThat(response.getYesCount()).isEqualTo(0);
        assertThat(response.getNoCount()).isEqualTo(0);
    }

    @Test
    void mapResultGettersAllProperties() {
        Agenda agenda = new Agenda("Reform", "Description");
        agenda.setId(4L);
        agenda.openSession(60L);

        AgendaResultDto result = new AgendaResultDto(10L, 5L, "APPROVED");
        VotingResultResponse response = mapper.toResultDTO(agenda, result);

        assertThat(response.getAgendaId()).isEqualTo(4L);
        assertThat(response.getTitle()).isEqualTo("Reform");
        assertThat(response.getYesCount()).isEqualTo(10);
        assertThat(response.getNoCount()).isEqualTo(5);
        assertThat(response.getResult()).isEqualTo("APPROVED");
    }
}
