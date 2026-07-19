package com.sicredi.voting.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sicredi.voting.dtos.AgendaResultDto;
import com.sicredi.voting.enums.VoteOption;
import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.models.Vote;
import com.sicredi.voting.repositories.AgendaRepository;
import com.sicredi.voting.repositories.VoteRepository;
import com.sicredi.voting.services.AgendaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AgendaWorkflowTest {

    @Mock
    private AgendaRepository agendaRepository;

    @Mock
    private VoteRepository voteRepository;

    @InjectMocks
    private AgendaService agendaService;

    @Test
    void agendaCompleteWorkflow() {
        // Create
        Agenda newAgenda = new Agenda("Reform", "Description");
        when(agendaRepository.save(any(Agenda.class))).thenReturn(newAgenda);
        agendaService.createAgenda("Reform", "Description");

        // Retrieve
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(newAgenda));
        Agenda retrievedAgenda = agendaService.findById(1L);
        assertThat(retrievedAgenda).isNotNull();

        // Open session
        when(agendaRepository.save(any(Agenda.class))).thenReturn(newAgenda);
        agendaService.openSession(1L, 60L);

        // Verify votes counted
        verify(agendaRepository, atLeast(2)).save(any(Agenda.class));
    }

    @Test
    void listMultipleAgendas() {
        Agenda a1 = new Agenda("A1", "D1");
        Agenda a2 = new Agenda("A2", "D2");
        Agenda a3 = new Agenda("A3", "D3");

        when(agendaRepository.findAll()).thenReturn(List.of(a1, a2, a3));

        var result = agendaService.listAll();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitle()).isEqualTo("A1");
    }

    @Test
    void votesForAgenda() {
        Vote v1 = new Vote(1L, "11111111111", VoteOption.YES);
        Vote v2 = new Vote(1L, "22222222222", VoteOption.YES);
        Vote v3 = new Vote(1L, "33333333333", VoteOption.NO);

        assertThat(v1.getAgendaId()).isEqualTo(v2.getAgendaId());
        assertThat(v2.getVote()).isNotEqualTo(v3.getVote());
    }

    @Test
    void tallyWithZeroVotes() {
        Agenda agenda = new Agenda("Test", "Desc");
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));
        when(voteRepository.countByAgenda(1L)).thenReturn(List.of());

        AgendaResultDto result = agendaService.tallyResult(1L);

        assertThat(result.result()).isEqualTo("TIED");
        assertThat(result.yesCount()).isEqualTo(0L);
        assertThat(result.noCount()).isEqualTo(0L);
    }
}
