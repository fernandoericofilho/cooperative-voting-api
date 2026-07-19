package com.sicredi.voting.controllers.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgendaResponse {
    private Long id;
    private String title;
    private String description;
    private String createdAt;
    private String sessionOpenedAt;
    private String sessionClosesAt;
    private String status;

    public AgendaResponse(Long id, String title, String description, String createdAt,
                    String sessionOpenedAt, String sessionClosesAt, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.sessionOpenedAt = sessionOpenedAt;
        this.sessionClosesAt = sessionClosesAt;
        this.status = status;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCreatedAt() { return createdAt; }
    public String getSessionOpenedAt() { return sessionOpenedAt; }
    public String getSessionClosesAt() { return sessionClosesAt; }
    public String getStatus() { return status; }
}
