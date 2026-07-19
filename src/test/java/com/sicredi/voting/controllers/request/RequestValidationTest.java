package com.sicredi.voting.controllers.request;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequestValidationTest {

    @Test
    void createAgendaRequestWithValues() {
        CreateAgendaRequest req = new CreateAgendaRequest("Reform", "Description");

        assertThat(req.title()).isEqualTo("Reform");
        assertThat(req.description()).isEqualTo("Description");
    }

    @Test
    void openSessionRequestWithDuration() {
        OpenSessionRequest req = new OpenSessionRequest(120L);

        assertThat(req.durationSeconds()).isEqualTo(120L);
    }

    @Test
    void openSessionRequestWithoutDuration() {
        OpenSessionRequest req = new OpenSessionRequest(null);

        assertThat(req.durationSeconds()).isNull();
    }

    @Test
    void registerVoteRequestValid() {
        RegisterVoteRequest req = new RegisterVoteRequest("12345678901", "YES");

        assertThat(req.memberCpf()).isEqualTo("12345678901");
        assertThat(req.vote()).isEqualTo("YES");
    }

    @Test
    void registerVoteRequestNo() {
        RegisterVoteRequest req = new RegisterVoteRequest("98765432109", "NO");

        assertThat(req.vote()).isEqualTo("NO");
    }
}
