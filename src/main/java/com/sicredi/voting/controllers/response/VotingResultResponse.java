package com.sicredi.voting.controllers.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VotingResultResponse {
    private Long agendaId;
    private String title;
    private Long yesCount;
    private Long noCount;
    private String result;
    private String status;

    public VotingResultResponse(Long agendaId, String title, Long yesCount, Long noCount, String result) {
        this.agendaId = agendaId;
        this.title = title;
        this.yesCount = yesCount;
        this.noCount = noCount;
        this.result = result;
    }

    public VotingResultResponse(Long agendaId, String title, Long yesCount, Long noCount, String result, String status) {
        this.agendaId = agendaId;
        this.title = title;
        this.yesCount = yesCount;
        this.noCount = noCount;
        this.result = result;
        this.status = status;
    }

    // Getters
    public Long getAgendaId() { return agendaId; }
    public String getTitle() { return title; }
    public Long getYesCount() { return yesCount; }
    public Long getNoCount() { return noCount; }
    public String getResult() { return result; }
    public String getStatus() { return status; }
}
