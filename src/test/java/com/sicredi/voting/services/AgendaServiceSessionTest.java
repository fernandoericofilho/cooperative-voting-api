package com.sicredi.voting.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sicredi.voting.exceptions.AgendaNotFoundException;
import com.sicredi.voting.exceptions.SessionAlreadyOpenException;
import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.repositories.AgendaRepository;
import com.sicredi.voting.repositories.VoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AgendaServiceSessionTest {

    @Mock
    private AgendaRepository agendaRepository;

    @Mock
    private VoteRepository voteRepository;

    @InjectMocks
    private AgendaService agendaService;

    @Test
    void openSessionSuccessfully() {
        Agenda agenda = new Agenda("Reform", "Desc");
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));
        when(agendaRepository.save(any(Agenda.class))).thenReturn(agenda);

        Agenda result = agendaService.openSession(1L, 60L);

        assertThat(result).isNotNull();
        assertThat(result.sessionIsOpen()).isTrue();
        verify(agendaRepository).save(any(Agenda.class));
    }

    @Test
    void openSessionWithDefaultDuration() {
        Agenda agenda = new Agenda("Reform", "Desc");
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));
        when(agendaRepository.save(any(Agenda.class))).thenReturn(agenda);

        Agenda result = agendaService.openSession(1L, null);

        assertThat(result).isNotNull();
        verify(agendaRepository).save(any(Agenda.class));
    }

    @Test
    void openSessionAlreadyOpenThrowsException() {
        Agenda agenda = new Agenda("Reform", "Desc");
        agenda.openSession(60L);
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));

        assertThatThrownBy(() -> agendaService.openSession(1L, 60L))
                .isInstanceOf(SessionAlreadyOpenException.class);
    }

    @Test
    void openSessionAgendaNotFoundThrowsException() {
        when(agendaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendaService.openSession(1L, 60L))
                .isInstanceOf(AgendaNotFoundException.class);
    }
}
