package com.sicredi.voting.models;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class AgendaTest {

    @Test
    void sessionNotOpenedInitially() {
        Agenda agenda = new Agenda("Reform", "Desc");

        assertThat(agenda.sessionWasOpened()).isFalse();
        assertThat(agenda.sessionIsOpen()).isFalse();
    }

    @Test
    void openSessionWithDuration() {
        Agenda agenda = new Agenda("Reform", "Desc");

        agenda.openSession(60);

        assertThat(agenda.sessionWasOpened()).isTrue();
        assertThat(agenda.sessionIsOpen()).isTrue();
        assertThat(agenda.getSessionOpenedAt()).isNotNull();
        assertThat(agenda.getSessionClosesAt()).isNotNull();
    }

    @Test
    void sessionDefaultDurationIsSixty() {
        Agenda agenda = new Agenda("Reform", "Desc");

        agenda.openSession(60L);

        LocalDateTime closesAt = agenda.getSessionClosesAt();
        LocalDateTime openedAt = agenda.getSessionOpenedAt();
        long secondsDifference = java.time.temporal.ChronoUnit.SECONDS.between(openedAt, closesAt);

        assertThat(secondsDifference).isEqualTo(60);
    }

    @Test
    void sessionClosedAfterExpiration() throws InterruptedException {
        Agenda agenda = new Agenda("Reform", "Desc");
        agenda.openSession(1);

        Thread.sleep(1100);

        assertThat(agenda.sessionIsClosed()).isTrue();
        assertThat(agenda.sessionIsOpen()).isFalse();
    }
}
