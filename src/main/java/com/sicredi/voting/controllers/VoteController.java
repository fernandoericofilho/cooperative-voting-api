package com.sicredi.voting.controllers;

import com.sicredi.voting.controllers.request.RegisterVoteRequest;
import com.sicredi.voting.controllers.response.VoteResponse;
import com.sicredi.voting.enums.VoteOption;
import com.sicredi.voting.mappers.VoteMapper;
import com.sicredi.voting.models.Vote;
import com.sicredi.voting.services.VoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agendas/{agendaId}/votes")
public class VoteController {

    private final VoteService voteService;
    private final VoteMapper voteMapper;

    public VoteController(VoteService voteService, VoteMapper voteMapper) {
        this.voteService = voteService;
        this.voteMapper = voteMapper;
    }

    @PostMapping
    public ResponseEntity<VoteResponse> registerVote(
            @PathVariable Long agendaId,
            @Valid @RequestBody RegisterVoteRequest request) {
        Vote vote = voteService.registerVote(agendaId, request.memberCpf(), VoteOption.valueOf(request.vote()));
        return ResponseEntity.status(HttpStatus.CREATED).body(voteMapper.toVoteDTO(vote));
    }
}
