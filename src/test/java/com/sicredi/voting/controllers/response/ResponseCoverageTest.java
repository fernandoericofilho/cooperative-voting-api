package com.sicredi.voting.controllers.response;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ResponseCoverageTest {

    @Test
    void agendaResponseAllGetters() {
        AgendaResponse response = new AgendaResponse(
            1L, "Reform", "Description", "2026-01-01T10:00:00",
            "2026-01-01T10:00:00", "2026-01-01T10:01:00", "OPEN"
        );

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Reform");
        assertThat(response.getDescription()).isEqualTo("Description");
        assertThat(response.getCreatedAt()).isEqualTo("2026-01-01T10:00:00");
        assertThat(response.getSessionOpenedAt()).isEqualTo("2026-01-01T10:00:00");
        assertThat(response.getSessionClosesAt()).isEqualTo("2026-01-01T10:01:00");
        assertThat(response.getStatus()).isEqualTo("OPEN");
    }

    @Test
    void agendaResponseWithNullValues() {
        AgendaResponse response = new AgendaResponse(
            2L, "Law", "Law Desc", "2026-01-01T10:00:00", null, null, "NOT_STARTED"
        );

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getTitle()).isEqualTo("Law");
        assertThat(response.getSessionOpenedAt()).isNull();
        assertThat(response.getSessionClosesAt()).isNull();
        assertThat(response.getStatus()).isEqualTo("NOT_STARTED");
    }

    @Test
    void voteResponseAllGetters() {
        VoteResponse response = new VoteResponse(
            1L, 1L, "12345678901", "YES", "2026-01-01T10:00:00"
        );

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAgendaId()).isEqualTo(1L);
        assertThat(response.getMemberCpf()).isEqualTo("12345678901");
        assertThat(response.getVote()).isEqualTo("YES");
        assertThat(response.getCreatedAt()).isEqualTo("2026-01-01T10:00:00");
    }

    @Test
    void voteResponseWithDifferentVote() {
        VoteResponse response = new VoteResponse(
            2L, 2L, "98765432100", "NO", "2026-01-01T11:00:00"
        );

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getAgendaId()).isEqualTo(2L);
        assertThat(response.getMemberCpf()).isEqualTo("98765432100");
        assertThat(response.getVote()).isEqualTo("NO");
        assertThat(response.getCreatedAt()).isEqualTo("2026-01-01T11:00:00");
    }

    @Test
    void votingResultResponseAllGetters() {
        VotingResultResponse response = new VotingResultResponse(
            1L, "Reform", 10L, 5L, "APPROVED"
        );

        assertThat(response.getAgendaId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Reform");
        assertThat(response.getYesCount()).isEqualTo(10L);
        assertThat(response.getNoCount()).isEqualTo(5L);
        assertThat(response.getResult()).isEqualTo("APPROVED");
    }

    @Test
    void votingResultResponseRejected() {
        VotingResultResponse response = new VotingResultResponse(
            2L, "Law", 3L, 8L, "REJECTED"
        );

        assertThat(response.getAgendaId()).isEqualTo(2L);
        assertThat(response.getYesCount()).isEqualTo(3L);
        assertThat(response.getNoCount()).isEqualTo(8L);
        assertThat(response.getResult()).isEqualTo("REJECTED");
    }

    @Test
    void agendaResponseConstructorCoverage() {
        AgendaResponse response = new AgendaResponse(
            5L, "Test Agenda", "Test Description", "2026-01-01T12:00:00",
            "2026-01-01T12:00:00", "2026-01-01T13:00:00", "OPEN"
        );

        assertThat(response)
            .hasFieldOrPropertyWithValue("id", 5L)
            .hasFieldOrPropertyWithValue("title", "Test Agenda")
            .hasFieldOrPropertyWithValue("description", "Test Description")
            .hasFieldOrPropertyWithValue("createdAt", "2026-01-01T12:00:00")
            .hasFieldOrPropertyWithValue("sessionOpenedAt", "2026-01-01T12:00:00")
            .hasFieldOrPropertyWithValue("sessionClosesAt", "2026-01-01T13:00:00")
            .hasFieldOrPropertyWithValue("status", "OPEN");
    }

    @Test
    void voteResponseConstructorCoverage() {
        VoteResponse response = new VoteResponse(
            10L, 5L, "11122233344", "YES", "2026-01-01T14:00:00"
        );

        assertThat(response)
            .hasFieldOrPropertyWithValue("id", 10L)
            .hasFieldOrPropertyWithValue("agendaId", 5L)
            .hasFieldOrPropertyWithValue("memberCpf", "11122233344")
            .hasFieldOrPropertyWithValue("vote", "YES")
            .hasFieldOrPropertyWithValue("createdAt", "2026-01-01T14:00:00");
    }

    @Test
    void votingResultResponseConstructorCoverage() {
        VotingResultResponse response = new VotingResultResponse(
            20L, "Agenda Title", 15L, 10L, "APPROVED"
        );

        assertThat(response)
            .hasFieldOrPropertyWithValue("agendaId", 20L)
            .hasFieldOrPropertyWithValue("title", "Agenda Title")
            .hasFieldOrPropertyWithValue("yesCount", 15L)
            .hasFieldOrPropertyWithValue("noCount", 10L)
            .hasFieldOrPropertyWithValue("result", "APPROVED");
    }

    @Test
    void pageResponseConstructor() {
        List<AgendaResponse> content = List.of(
            new AgendaResponse(1L, "Agenda1", "Desc1", "2026-01-01T10:00:00",
                              "2026-01-01T10:00:00", "2026-01-01T11:00:00", "OPEN")
        );

        PageResponse<AgendaResponse> response = new PageResponse<>(
            content, 50L, 3, 1, 20, true, false
        );

        assertThat(response.content()).isEqualTo(content);
        assertThat(response.totalElements()).isEqualTo(50L);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.currentPage()).isEqualTo(1);
        assertThat(response.pageSize()).isEqualTo(20);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.hasPrevious()).isFalse();
    }

    @Test
    void pageResponseWithMultipleAgendas() {
        List<AgendaResponse> content = List.of(
            new AgendaResponse(1L, "Agenda1", "Desc1", "2026-01-01T10:00:00",
                              "2026-01-01T10:00:00", "2026-01-01T11:00:00", "OPEN"),
            new AgendaResponse(2L, "Agenda2", "Desc2", "2026-01-01T10:00:00",
                              "2026-01-01T10:00:00", "2026-01-01T11:00:00", "CLOSED")
        );

        PageResponse<AgendaResponse> response = new PageResponse<>(
            content, 200L, 10, 2, 20, true, true
        );

        assertThat(response.content()).hasSize(2);
        assertThat(response.totalElements()).isEqualTo(200L);
        assertThat(response.currentPage()).isEqualTo(2);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.hasPrevious()).isTrue();
    }
}
