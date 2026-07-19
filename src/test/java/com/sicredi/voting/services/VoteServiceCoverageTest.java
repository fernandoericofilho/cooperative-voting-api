package com.sicredi.voting.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sicredi.voting.enums.VoteOption;
import com.sicredi.voting.enums.VotingStatus;
import com.sicredi.voting.exceptions.MemberNotEligibleException;
import com.sicredi.voting.exceptions.SessionClosedException;
import com.sicredi.voting.exceptions.SessionNotOpenException;
import com.sicredi.voting.exceptions.DuplicateVoteException;
import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.models.Vote;
import com.sicredi.voting.repositories.VoteRepository;
import com.sicredi.voting.services.external.UserInfoClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class VoteServiceCoverageTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private AgendaService agendaService;

    @Mock
    private UserInfoClient userInfoClient;

    @InjectMocks
    private VoteService voteService;

    @Test
    void registerVoteSuccessfullyYes() {
        Agenda agenda = new Agenda("Reform", "Desc");
        agenda.openSession(60L);

        when(agendaService.findById(1L)).thenReturn(agenda);
        when(userInfoClient.check("12345678901")).thenReturn(VotingStatus.ELIGIBLE);

        Vote vote = new Vote(1L, "12345678901", VoteOption.YES);
        vote.setId(1L);
        when(voteRepository.save(any(Vote.class))).thenReturn(vote);

        Vote result = voteService.registerVote(1L, "12345678901", VoteOption.YES);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(voteRepository).save(any(Vote.class));
    }

    @Test
    void registerVoteSuccessfullyNo() {
        Agenda agenda = new Agenda("Reform", "Desc");
        agenda.openSession(60L);

        when(agendaService.findById(1L)).thenReturn(agenda);
        when(userInfoClient.check("98765432100")).thenReturn(VotingStatus.ELIGIBLE);

        Vote vote = new Vote(1L, "98765432100", VoteOption.NO);
        vote.setId(2L);
        when(voteRepository.save(any(Vote.class))).thenReturn(vote);

        Vote result = voteService.registerVote(1L, "98765432100", VoteOption.NO);

        assertThat(result).isNotNull();
        assertThat(result.getVote()).isEqualTo(VoteOption.NO);
    }

    @Test
    void registerVoteSessionNotOpen() {
        Agenda agenda = new Agenda("Reform", "Desc");

        when(agendaService.findById(1L)).thenReturn(agenda);

        assertThatThrownBy(() -> voteService.registerVote(1L, "12345678901", VoteOption.YES))
            .isInstanceOf(SessionNotOpenException.class);
    }

    @Test
    void registerVoteSessionClosed() {
        Agenda agenda = new Agenda("Reform", "Desc");
        agenda.openSession(1L);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        when(agendaService.findById(1L)).thenReturn(agenda);

        assertThatThrownBy(() -> voteService.registerVote(1L, "12345678901", VoteOption.YES))
            .isInstanceOf(SessionClosedException.class);
    }

    @Test
    void registerVoteMemberNotEligible() {
        Agenda agenda = new Agenda("Reform", "Desc");
        agenda.openSession(60L);

        when(agendaService.findById(1L)).thenReturn(agenda);
        when(userInfoClient.check("12345678901")).thenReturn(VotingStatus.NOT_ELIGIBLE);

        assertThatThrownBy(() -> voteService.registerVote(1L, "12345678901", VoteOption.YES))
            .isInstanceOf(MemberNotEligibleException.class);
    }

    @Test
    void registerVoteDuplicate() {
        Agenda agenda = new Agenda("Reform", "Desc");
        agenda.openSession(60L);

        when(agendaService.findById(1L)).thenReturn(agenda);
        when(userInfoClient.check("12345678901")).thenReturn(VotingStatus.ELIGIBLE);
        when(voteRepository.save(any(Vote.class))).thenThrow(new DataIntegrityViolationException("Duplicate"));

        assertThatThrownBy(() -> voteService.registerVote(1L, "12345678901", VoteOption.YES))
            .isInstanceOf(DuplicateVoteException.class);
    }

    @Test
    void registerVoteWithMultipleCPFs() {
        Agenda agenda = new Agenda("Reform", "Desc");
        agenda.openSession(60L);

        when(agendaService.findById(2L)).thenReturn(agenda);
        when(userInfoClient.check("11122233344")).thenReturn(VotingStatus.ELIGIBLE);

        Vote vote = new Vote(2L, "11122233344", VoteOption.YES);
        vote.setId(3L);
        when(voteRepository.save(any(Vote.class))).thenReturn(vote);

        Vote result = voteService.registerVote(2L, "11122233344", VoteOption.YES);

        assertThat(result).isNotNull();
        verify(voteRepository).save(any(Vote.class));
    }

    @Test
    void registerVoteVerifyLoggingInfo() {
        Agenda agenda = new Agenda("Reform", "Desc");
        agenda.openSession(60L);

        when(agendaService.findById(1L)).thenReturn(agenda);
        when(userInfoClient.check("12345678901")).thenReturn(VotingStatus.ELIGIBLE);

        Vote vote = new Vote(1L, "12345678901", VoteOption.YES);
        vote.setId(1L);
        when(voteRepository.save(any(Vote.class))).thenReturn(vote);

        voteService.registerVote(1L, "12345678901", VoteOption.YES);

        verify(userInfoClient).check("12345678901");
    }
}
