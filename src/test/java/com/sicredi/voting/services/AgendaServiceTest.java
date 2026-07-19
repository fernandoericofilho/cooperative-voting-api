package com.sicredi.voting.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sicredi.voting.dtos.AgendaResultDto;
import com.sicredi.voting.enums.VoteOption;
import com.sicredi.voting.exceptions.AgendaNotFoundException;
import com.sicredi.voting.exceptions.SessionAlreadyOpenException;
import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.models.Vote;
import com.sicredi.voting.repositories.AgendaRepository;
import com.sicredi.voting.repositories.VoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    @Mock
    private AgendaRepository agendaRepository;

    @Mock
    private VoteRepository voteRepository;

    @InjectMocks
    private AgendaService agendaService;

    @Test
    void createAgendaPersists() {
        Agenda expected = new Agenda("Reform", "Discussion about article 5");
        when(agendaRepository.save(any(Agenda.class))).thenReturn(expected);

        Agenda result = agendaService.createAgenda("Reform", "Discussion about article 5");

        assertThat(result).isEqualTo(expected);
        verify(agendaRepository).save(any(Agenda.class));
    }

    @Test
    void findByIdReturnsAgenda() {
        Agenda expected = new Agenda("Reform", "Desc");
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(expected));

        Agenda result = agendaService.findById(1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void findByIdNotFoundThrowsException() {
        when(agendaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendaService.findById(999L))
                .isInstanceOf(AgendaNotFoundException.class);
    }

    @Test
    void openSessionWithDefaultDuration() {
        Agenda agenda = new Agenda("Reform", "Desc");
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));
        when(agendaRepository.save(any(Agenda.class))).thenReturn(agenda);

        agendaService.openSession(1L, 60L);

        verify(agendaRepository).save(any(Agenda.class));
        assertThat(agenda.sessionIsOpen()).isTrue();
    }

    @Test
    void openSessionTwiceThrowsException() {
        Agenda agenda = new Agenda("Reform", "Desc");
        agenda.openSession(60L);
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));

        assertThatThrownBy(() -> agendaService.openSession(1L, 60L))
                .isInstanceOf(SessionAlreadyOpenException.class);
    }

    @Test
    void listAgendasReturnsPage() {
        Agenda agenda = new Agenda("Reform", "Desc");
        Page<Agenda> expected = new PageImpl<>(List.of(agenda), PageRequest.of(0, 10), 1);
        when(agendaRepository.findAll(any(PageRequest.class))).thenReturn(expected);

        Page<Agenda> result = agendaService.listAgendas(PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).contains(agenda);
    }

    @Test
    void tallyResultWithYesVotes() {
        Agenda agenda = new Agenda("Reform", "Desc");
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));
        com.sicredi.voting.dtos.VoteCount count1 = mock(com.sicredi.voting.dtos.VoteCount.class);
        com.sicredi.voting.dtos.VoteCount count2 = mock(com.sicredi.voting.dtos.VoteCount.class);
        when(count1.getOption()).thenReturn(VoteOption.YES);
        when(count1.getTotal()).thenReturn(2L);
        when(count2.getOption()).thenReturn(VoteOption.NO);
        when(count2.getTotal()).thenReturn(1L);
        when(voteRepository.countByAgenda(1L)).thenReturn(List.of(count1, count2));

        AgendaResultDto result = agendaService.tallyResult(1L);

        assertThat(result).isNotNull();
    }
}
