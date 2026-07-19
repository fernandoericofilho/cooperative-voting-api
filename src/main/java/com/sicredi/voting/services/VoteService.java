package com.sicredi.voting.services;

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
import com.sicredi.voting.services.util.CpfUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final AgendaService agendaService;
    private final UserInfoClient userInfoClient;

    public VoteService(VoteRepository voteRepository, AgendaService agendaService, UserInfoClient userInfoClient) {
        this.voteRepository = voteRepository;
        this.agendaService = agendaService;
        this.userInfoClient = userInfoClient;
    }

    public Vote registerVote(Long agendaId, String memberCpf, VoteOption vote) {
        String maskedCpf = CpfUtils.mask(memberCpf);
        Agenda agenda = agendaService.findById(agendaId);

        if (!agenda.sessionWasOpened()) {
            log.warn("Attempt to vote with session not opened: agendaId={}, member={}", agendaId, maskedCpf);
            throw new SessionNotOpenException(agendaId);
        }
        if (agenda.sessionIsClosed()) {
            log.warn("Attempt to vote with session closed: agendaId={}, member={}", agendaId, maskedCpf);
            throw new SessionClosedException(agendaId);
        }
        VotingStatus status = userInfoClient.check(memberCpf);
        if (status == VotingStatus.NOT_ELIGIBLE) {
            log.warn("Member not eligible to vote: agendaId={}, member={}, status={}", agendaId, maskedCpf, status);
            throw new MemberNotEligibleException(memberCpf);
        }
        log.info("Eligibility verified: agendaId={}, member={}, status={}", agendaId, maskedCpf, status);

        try {
            Vote registeredVote = voteRepository.save(new Vote(agendaId, memberCpf, vote));
            log.info("Vote registered successfully: agendaId={}, member={}, vote={}, voteId={}", agendaId, maskedCpf, vote, registeredVote.getId());
            return registeredVote;
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate vote detected (unique constraint violation): agendaId={}, member={}", agendaId, maskedCpf);
            throw new DuplicateVoteException(agendaId, memberCpf);
        }
    }
}
