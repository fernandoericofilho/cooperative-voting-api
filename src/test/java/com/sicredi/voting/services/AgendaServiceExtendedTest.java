package com.sicredi.voting.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.repositories.AgendaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AgendaServiceExtendedTest {

    @Mock
    private AgendaRepository agendaRepository;

    @InjectMocks
    private AgendaService agendaService;

    @Test
    void findAgendaById() {
        Agenda agenda = new Agenda("Reform", "Desc");
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));

        Agenda result = agendaService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Reform");
    }

    @Test
    void createAgenda() {
        Agenda agenda = new Agenda("Agenda Test", "Description Test");
        when(agendaRepository.save(any(Agenda.class))).thenReturn(agenda);

        Agenda result = agendaService.createAgenda("Agenda Test", "Description Test");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Agenda Test");
        verify(agendaRepository).save(any(Agenda.class));
    }
}
