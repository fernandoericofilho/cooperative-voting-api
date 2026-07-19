package com.sicredi.voting.models;

import com.sicredi.voting.enums.VoteOption;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "vote", uniqueConstraints = @UniqueConstraint(name = "uk_vote_agenda_member_cpf", columnNames = {"agenda_id", "member_cpf"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agenda_id", nullable = false)
    private Long agendaId;

    @Column(name = "member_cpf", nullable = false, length = 11, columnDefinition = "CHAR(11)")
    private String memberCpf;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote", nullable = false, length = 3)
    private VoteOption vote;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Vote(Long agendaId, String memberCpf, VoteOption vote) {
        this.agendaId = agendaId;
        this.memberCpf = memberCpf;
        this.vote = vote;
        this.createdAt = LocalDateTime.now();
    }
}
