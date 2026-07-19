package com.sicredi.voting.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sicredi.voting.dtos.VoteCount;
import com.sicredi.voting.dtos.AgendaResultDto;
import com.sicredi.voting.enums.VoteOption;
import com.sicredi.voting.exceptions.AgendaNotFoundException;
import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.repositories.AgendaRepository;
import com.sicredi.voting.repositories.VoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AgendaServiceResultTest {

    @Mock
    private AgendaRepository agendaRepository;

    @Mock
    private VoteRepository voteRepository;

    @InjectMocks
    private AgendaService agendaService;

    private static class VoteCountMock implements VoteCount {
        private final VoteOption option;
        private final Long total;

        VoteCountMock(VoteOption option, Long total) {
            this.option = option;
            this.total = total;
        }

        @Override
        public VoteOption getOption() {
            return option;
        }

        @Override
        public Long getTotal() {
            return total;
        }
    }

    @Test
    void tallyResultApproved() {
        Agenda agenda = new Agenda("Reform", "Desc");
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));
        when(voteRepository.countByAgenda(1L)).thenReturn(List.of(
            new VoteCountMock(VoteOption.YES, 5L),
            new VoteCountMock(VoteOption.NO, 3L)
        ));

        AgendaResultDto result = agendaService.tallyResult(1L);

        assertThat(result).isNotNull();
        assertThat(result.yesCount()).isEqualTo(5L);
        assertThat(result.noCount()).isEqualTo(3L);
        assertThat(result.result()).isEqualTo("APPROVED");
    }

    @Test
    void tallyResultRejected() {
        Agenda agenda = new Agenda("Reform", "Desc");
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));
        when(voteRepository.countByAgenda(1L)).thenReturn(List.of(
            new VoteCountMock(VoteOption.YES, 2L),
            new VoteCountMock(VoteOption.NO, 8L)
        ));

        AgendaResultDto result = agendaService.tallyResult(1L);

        assertThat(result.result()).isEqualTo("REJECTED");
    }

    @Test
    void tallyResultTie() {
        Agenda agenda = new Agenda("Reform", "Desc");
        when(agendaRepository.findById(1L)).thenReturn(Optional.of(agenda));
        when(voteRepository.countByAgenda(1L)).thenReturn(List.of(
            new VoteCountMock(VoteOption.YES, 5L),
            new VoteCountMock(VoteOption.NO, 5L)
        ));

        AgendaResultDto result = agendaService.tallyResult(1L);

        assertThat(result.result()).isEqualTo("TIED");
    }

    @Test
    void tallyResultAgendaNotFoundThrowsException() {
        when(agendaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendaService.tallyResult(1L))
                .isInstanceOf(AgendaNotFoundException.class);
    }

    @Test
    void listAllAgendas() {
        Agenda agenda1 = new Agenda("Reform", "Desc1");
        Agenda agenda2 = new Agenda("Law", "Desc2");
        when(agendaRepository.findAll()).thenReturn(List.of(agenda1, agenda2));

        var result = agendaService.listAll();

        assertThat(result).hasSize(2);
        verify(agendaRepository).findAll();
    }
}
