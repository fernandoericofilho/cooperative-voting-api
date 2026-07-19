package com.sicredi.voting.mappers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.voting.controllers.response.AgendaResponse;
import com.sicredi.voting.models.Agenda;
import org.junit.jupiter.api.Test;

class AgendaMapperSimpleTest {

    private final AgendaMapper mapper = new AgendaMapper();

    @Test
    void mapAgendaNotStarted() {
        Agenda agenda = new Agenda("Title", "Description");

        AgendaResponse response = mapper.toAgendaDTO(agenda);

        assertThat(response.getTitle()).isEqualTo("Title");
        assertThat(response.getDescription()).isEqualTo("Description");
        assertThat(response.getStatus()).isEqualTo("NOT_STARTED");
        assertThat(response.getSessionOpenedAt()).isNull();
        assertThat(response.getSessionClosesAt()).isNull();
    }

    @Test
    void mapAgendaClosed() {
        Agenda agenda = new Agenda("Title", "Description");
        agenda.openSession(1L);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
        }

        AgendaResponse response = mapper.toAgendaDTO(agenda);

        assertThat(response.getStatus()).isEqualTo("CLOSED");
        assertThat(response.getSessionClosesAt()).isNotNull();
    }

    @Test
    void mapAgendaWithId() {
        Agenda agenda = new Agenda("Title", "Description");
        agenda.setId(123L);

        AgendaResponse response = mapper.toAgendaDTO(agenda);

        assertThat(response.getId()).isEqualTo(123L);
    }

    @Test
    void mapAgendaCreatedAtNotNull() {
        Agenda agenda = new Agenda("Title", "Description");

        AgendaResponse response = mapper.toAgendaDTO(agenda);

        assertThat(response.getCreatedAt()).isNotNull();
    }
}
