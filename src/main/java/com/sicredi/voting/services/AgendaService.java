package com.sicredi.voting.services;

import com.sicredi.voting.dtos.VoteCount;
import com.sicredi.voting.dtos.AgendaResultDto;
import com.sicredi.voting.enums.VoteOption;
import com.sicredi.voting.exceptions.AgendaNotFoundException;
import com.sicredi.voting.exceptions.SessionAlreadyOpenException;
import com.sicredi.voting.models.Agenda;
import com.sicredi.voting.repositories.AgendaRepository;
import com.sicredi.voting.repositories.VoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgendaService {

    private static final long DEFAULT_DURATION_SECONDS = 60L;

    private final AgendaRepository agendaRepository;
    private final VoteRepository voteRepository;

    public AgendaService(AgendaRepository agendaRepository, VoteRepository voteRepository) {
        this.agendaRepository = agendaRepository;
        this.voteRepository = voteRepository;
    }

    public Agenda createAgenda(String title, String description) {
        Agenda agenda = agendaRepository.save(new Agenda(title, description));
        log.info("Agenda created: id={}, title={}", agenda.getId(), title);
        return agenda;
    }

    public Agenda findById(Long id) {
        return agendaRepository.findById(id)
            .orElseThrow(() -> new AgendaNotFoundException(id));
    }

    public java.util.List<Agenda> listAll() {
        return agendaRepository.findAll();
    }

    public Page<Agenda> listAgendas(Pageable pageable) {
        log.info("Listing agendas: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return agendaRepository.findAll(pageable);
    }

    public Agenda openSession(Long agendaId, Long durationSeconds) {
        Agenda agenda = findById(agendaId);
        if (agenda.sessionWasOpened()) {
            log.warn("Attempt to open already opened session: agendaId={}", agendaId);
            throw new SessionAlreadyOpenException(agendaId);
        }
        long duration = durationSeconds != null ? durationSeconds : DEFAULT_DURATION_SECONDS;
        agenda.openSession(duration);
        Agenda savedAgenda = agendaRepository.save(agenda);
        log.info("Session opened: agendaId={}, duration={}s", agendaId, duration);
        return savedAgenda;
    }

    public AgendaResultDto tallyResult(Long agendaId) {
        findById(agendaId);
        long yesCount = 0L;
        long noCount = 0L;
        for (VoteCount count : voteRepository.countByAgenda(agendaId)) {
            if (count.getOption() == VoteOption.YES) {
                yesCount = count.getTotal();
            } else {
                noCount = count.getTotal();
            }
        }
        return AgendaResultDto.calculate(yesCount, noCount);
    }
}
