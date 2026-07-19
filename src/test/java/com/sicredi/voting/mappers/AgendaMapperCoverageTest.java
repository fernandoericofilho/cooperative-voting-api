package com.sicredi.voting.mappers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.voting.controllers.response.AgendaResponse;
import com.sicredi.voting.models.Agenda;
import org.junit.jupiter.api.Test;

class AgendaMapperCoverageTest {

    private final AgendaMapper mapper = new AgendaMapper();

    @Test
    void mapAgendaOpen() {
        Agenda agenda = new Agenda("Reform", "Description");
        agenda.openSession(600L);

        AgendaResponse response = mapper.toAgendaDTO(agenda);

        assertThat(response.getStatus()).isEqualTo("OPEN");
        assertThat(response.getSessionOpenedAt()).isNotNull();
        assertThat(response.getSessionClosesAt()).isNotNull();
    }

    @Test
    void mapAgendaOpenTitle() {
        Agenda agenda = new Agenda("Fundamental Law", "Law description");
        agenda.openSession(60L);

        AgendaResponse response = mapper.toAgendaDTO(agenda);

        assertThat(response.getTitle()).isEqualTo("Fundamental Law");
        assertThat(response.getDescription()).isEqualTo("Law description");
        assertThat(response.getStatus()).isEqualTo("OPEN");
    }

    @Test
    void mapAgendaOpenWithId() {
        Agenda agenda = new Agenda("Voting", "Description");
        agenda.setId(100L);
        agenda.openSession(1000L);

        AgendaResponse response = mapper.toAgendaDTO(agenda);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo("OPEN");
    }

    @Test
    void mapAgendaOpenSessionInfo() {
        Agenda agenda = new Agenda("Reform", "Description");
        agenda.openSession(500L);

        AgendaResponse response = mapper.toAgendaDTO(agenda);

        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getSessionOpenedAt()).isNotNull();
        assertThat(response.getSessionClosesAt()).isNotNull();
        assertThat(response.getStatus()).isEqualTo("OPEN");
    }

    @Test
    void mapAgendaOpenFormatting() {
        Agenda agenda = new Agenda("Test", "Test Desc");
        agenda.openSession(60L);

        AgendaResponse response = mapper.toAgendaDTO(agenda);

        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getSessionOpenedAt()).isNotNull();
        assertThat(response.getStatus()).isEqualTo("OPEN");
    }

    @Test
    void mapAgendaOpenTimeFormats() {
        Agenda agenda = new Agenda("Law", "Desc");
        agenda.openSession(120L);

        AgendaResponse response = mapper.toAgendaDTO(agenda);

        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getSessionOpenedAt()).isNotNull();
        assertThat(response.getSessionClosesAt()).isNotNull();
    }

    @Test
    void mapAgendaOpenGettersAllProperties() {
        Agenda agenda = new Agenda("Reform", "Description");
        agenda.setId(5L);
        agenda.openSession(60L);

        AgendaResponse response = mapper.toAgendaDTO(agenda);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getTitle()).isEqualTo("Reform");
        assertThat(response.getDescription()).isEqualTo("Description");
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getSessionOpenedAt()).isNotNull();
        assertThat(response.getSessionClosesAt()).isNotNull();
        assertThat(response.getStatus()).isEqualTo("OPEN");
    }
}
