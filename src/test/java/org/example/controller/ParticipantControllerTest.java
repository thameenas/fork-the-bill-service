package org.example.controller;

import org.example.model.Participant;
import org.example.service.ParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ParticipantControllerTest {
    @Mock
    private ParticipantService participantService;

    @InjectMocks
    private ParticipantController participantController;

    private Participant participant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        participant = new Participant();
        participant.setId("p1");
        participant.setName("Alice");
        participant.setSelectedItems(Collections.singletonList("item-1"));
    }

    @Test
    void testCreateParticipant() {
        when(participantService.createParticipant(any(Participant.class))).thenReturn(participant);
        ResponseEntity<Participant> response = participantController.createParticipant(participant);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Alice", response.getBody().getName());
    }

    @Test
    void testGetAllParticipants() {
        when(participantService.getAllParticipants()).thenReturn(Collections.singletonList(participant));
        ResponseEntity<List<Participant>> response = participantController.getAllParticipants();
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetParticipantById() {
        when(participantService.getParticipantById(eq("p1"))).thenReturn(Optional.of(participant));
        ResponseEntity<Participant> response = participantController.getParticipant("p1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Alice", response.getBody().getName());
    }

    @Test
    void testGetParticipantByIdNotFound() {
        when(participantService.getParticipantById(eq("notfound"))).thenReturn(Optional.empty());
        ResponseEntity<Participant> response = participantController.getParticipant("notfound");
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateParticipant() {
        Participant updated = new Participant();
        updated.setId("p1");
        updated.setName("Bob");
        updated.setSelectedItems(Collections.singletonList("item-2"));
        when(participantService.updateParticipant(eq("p1"), any(Participant.class))).thenReturn(updated);
        ResponseEntity<Participant> response = participantController.updateParticipant("p1", updated);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Bob", response.getBody().getName());
    }

    @Test
    void testUpdateParticipantNotFound() {
        when(participantService.updateParticipant(eq("notfound"), any(Participant.class))).thenThrow(new RuntimeException("Participant not found"));
        ResponseEntity<Participant> response = participantController.updateParticipant("notfound", participant);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteParticipant() {
        doNothing().when(participantService).deleteParticipant(eq("p1"));
        ResponseEntity<Void> response = participantController.deleteParticipant("p1");
        assertEquals(204, response.getStatusCode().value());
    }
}
