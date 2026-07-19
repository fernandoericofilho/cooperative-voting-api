package com.sicredi.voting.models;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class AgendaCompleteTest {

    @Test
    void agendaTitleDescription() {
        Agenda agenda = new Agenda("Long Title", "Detailed Description");

        assertThat(agenda.getTitle()).isEqualTo("Long Title");
        assertThat(agenda.getDescription()).isEqualTo("Detailed Description");
    }

    @Test
    void agendaCreatedAtNotNull() {
        Agenda agenda = new Agenda("Test", "Desc");

        assertThat(agenda.getCreatedAt()).isNotNull();
        assertThat(agenda.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void agendaIdNull() {
        Agenda agenda = new Agenda("Test", "Desc");

        assertThat(agenda.getId()).isNull();
    }

    @Test
    void agendaIdSettable() {
        Agenda agenda = new Agenda("Test", "Desc");
        agenda.setId(999L);

        assertThat(agenda.getId()).isEqualTo(999L);
    }

    @Test
    void agendaSessionFieldsNullInitially() {
        Agenda agenda = new Agenda("Test", "Desc");

        assertThat(agenda.getSessionOpenedAt()).isNull();
        assertThat(agenda.getSessionClosesAt()).isNull();
    }

    @Test
    void agendaOpenSessionSetsTimestamps() {
        Agenda agenda = new Agenda("Test", "Desc");
        LocalDateTime before = LocalDateTime.now();

        agenda.openSession(60L);

        assertThat(agenda.getSessionOpenedAt()).isNotNull();
        assertThat(agenda.getSessionClosesAt()).isNotNull();
        assertThat(agenda.getSessionOpenedAt()).isAfterOrEqualTo(before);
        assertThat(agenda.getSessionClosesAt()).isAfter(agenda.getSessionOpenedAt());
    }

    @Test
    void agendaDurationZero() {
        Agenda agenda = new Agenda("Test", "Desc");
        agenda.openSession(0L);

        assertThat(agenda.getSessionClosesAt()).isEqualTo(agenda.getSessionOpenedAt());
    }

    @Test
    void agendaDurationLong() {
        Agenda agenda = new Agenda("Test", "Desc");
        agenda.openSession(86400L);

        assertThat(agenda.getSessionClosesAt()).isAfter(agenda.getSessionOpenedAt().plusHours(23));
    }

    @Test
    void agendaTitleSettable() {
        Agenda agenda = new Agenda("Original", "Desc");
        agenda.setTitle("New Title");

        assertThat(agenda.getTitle()).isEqualTo("New Title");
    }

    @Test
    void agendaDescriptionSettable() {
        Agenda agenda = new Agenda("Test", "Original");
        agenda.setDescription("New Description");

        assertThat(agenda.getDescription()).isEqualTo("New Description");
    }

    @Test
    void agendaCreatedAtSettable() {
        Agenda agenda = new Agenda("Test", "Desc");
        LocalDateTime before = agenda.getCreatedAt();

        LocalDateTime newTime = before.minusHours(1);
        agenda.setCreatedAt(newTime);

        assertThat(agenda.getCreatedAt()).isNotEqualTo(before);
        assertThat(agenda.getCreatedAt()).isEqualTo(newTime);
    }
}
