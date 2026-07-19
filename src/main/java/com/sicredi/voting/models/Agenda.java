package com.sicredi.voting.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "agenda")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "session_opened_at")
    private LocalDateTime sessionOpenedAt;

    @Column(name = "session_closes_at")
    private LocalDateTime sessionClosesAt;

    public Agenda(String title, String description) {
        this.title = title;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public boolean sessionWasOpened() {
        return sessionOpenedAt != null;
    }

    public boolean sessionIsOpen() {
        return sessionWasOpened() && LocalDateTime.now().isBefore(sessionClosesAt);
    }

    public boolean sessionIsClosed() {
        return sessionWasOpened() && !sessionIsOpen();
    }

    public void openSession(long durationSeconds) {
        this.sessionOpenedAt = LocalDateTime.now();
        this.sessionClosesAt = this.sessionOpenedAt.plusSeconds(durationSeconds);
    }

    public String getStatus() {
        if (!sessionWasOpened()) {
            return "NOT_STARTED";
        }
        return sessionIsOpen() ? "OPEN" : "CLOSED";
    }
}
