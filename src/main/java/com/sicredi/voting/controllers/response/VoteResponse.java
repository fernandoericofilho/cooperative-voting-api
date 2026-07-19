package com.sicredi.voting.controllers.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoteResponse {
    private Long id;
    private Long agendaId;
    private String memberCpf;
    private String vote;
    private String createdAt;

    public VoteResponse(Long id, Long agendaId, String memberCpf, String vote, String createdAt) {
        this.id = id;
        this.agendaId = agendaId;
        this.memberCpf = memberCpf;
        this.vote = vote;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getId() { return id; }
    public Long getAgendaId() { return agendaId; }
    public String getMemberCpf() { return memberCpf; }
    public String getVote() { return vote; }
    public String getCreatedAt() { return createdAt; }
}
