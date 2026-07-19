package com.sicredi.voting.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Agenda Comprehensive Coverage Tests")
class AgendaComprehensiveTest {

    @Test
    @DisplayName("Should set id through setter")
    void testSetId() {
        Agenda agenda = new Agenda("Title", "Description");
        agenda.setId(99L);
        assertEquals(99L, agenda.getId());
    }

    @Test
    @DisplayName("Should set title through setter")
    void testSetTitle() {
        Agenda agenda = new Agenda("Original", "Description");
        agenda.setTitle("Updated");
        assertEquals("Updated", agenda.getTitle());
    }

    @Test
    @DisplayName("Should set description through setter")
    void testSetDescription() {
        Agenda agenda = new Agenda("Title", "Original");
        agenda.setDescription("Updated");
        assertEquals("Updated", agenda.getDescription());
    }

    @Test
    @DisplayName("Should set createdAt through setter")
    void testSetCreatedAt() {
        Agenda agenda = new Agenda("Title", "Description");
        LocalDateTime now = LocalDateTime.now();
        agenda.setCreatedAt(now);
        assertEquals(now, agenda.getCreatedAt());
    }

    @Test
    @DisplayName("Should set sessionOpenedAt through setter")
    void testSetSessionOpenedAt() {
        Agenda agenda = new Agenda("Title", "Description");
        LocalDateTime now = LocalDateTime.now();
        agenda.setSessionOpenedAt(now);
        assertEquals(now, agenda.getSessionOpenedAt());
    }

    @Test
    @DisplayName("Should set sessionClosesAt through setter")
    void testSetSessionClosesAt() {
        Agenda agenda = new Agenda("Title", "Description");
        LocalDateTime now = LocalDateTime.now();
        agenda.setSessionClosesAt(now);
        assertEquals(now, agenda.getSessionClosesAt());
    }

    @Test
    @DisplayName("Should calculate NOT_STARTED status correctly")
    void testStatusNotStarted() {
        Agenda agenda = new Agenda("Title", "Description");
        assertEquals("NOT_STARTED", agenda.getStatus());
    }

    @Test
    @DisplayName("Should calculate OPEN status correctly")
    void testStatusOpen() {
        Agenda agenda = new Agenda("Title", "Description");
        agenda.openSession(60);
        assertEquals("OPEN", agenda.getStatus());
    }

    @Test
    @DisplayName("Should calculate CLOSED status when past closing time")
    void testStatusClosed() {
        Agenda agenda = new Agenda("Title", "Description");
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        agenda.setSessionOpenedAt(pastTime);
        agenda.setSessionClosesAt(pastTime.minusSeconds(1));
        assertEquals("CLOSED", agenda.getStatus());
    }
}
