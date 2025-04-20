package org.example.service;

import org.example.model.Participant;
import org.example.repository.ParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ParticipantServiceTest {
    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private ParticipantService participantService;

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
        when(participantRepository.save(any(Participant.class))).thenReturn(participant);
        Participant created = participantService.createParticipant(participant);
        assertEquals(participant, created);
    }

    @Test
    void testGetAllParticipants() {
        when(participantRepository.findAll()).thenReturn(Collections.singletonList(participant));
        List<Participant> participants = participantService.getAllParticipants();
        assertEquals(1, participants.size());
        assertEquals(participant, participants.getFirst());
    }

    @Test
    void testGetParticipantById() {
        when(participantRepository.findById(eq("p1"))).thenReturn(Optional.of(participant));
        Optional<Participant> found = participantService.getParticipantById("p1");
        assertTrue(found.isPresent());
        assertEquals(participant, found.get());
    }

    @Test
    void testUpdateParticipant() {
        Participant updated = new Participant();
        updated.setId("p1");
        updated.setName("Bob");
        updated.setSelectedItems(Collections.singletonList("item-2"));
        when(participantRepository.findById(eq("p1"))).thenReturn(Optional.of(participant));
        when(participantRepository.save(any(Participant.class))).thenReturn(updated);
        Participant result = participantService.updateParticipant("p1", updated);
        assertEquals("Bob", result.getName());
        assertEquals(Collections.singletonList("item-2"), result.getSelectedItems());
    }

    @Test
    void testDeleteParticipant() {
        doNothing().when(participantRepository).deleteById(eq("p1"));
        participantService.deleteParticipant("p1");
        verify(participantRepository, times(1)).deleteById("p1");
    }
}
