package org.example.service;

import org.example.model.Participant;
import org.example.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParticipantService {
    @Autowired
    private ParticipantRepository participantRepository;

    public Participant createParticipant(Participant participant) {
        return participantRepository.save(participant);
    }

    public List<Participant> getAllParticipants() {
        return participantRepository.findAll();
    }

    public Optional<Participant> getParticipantById(String id) {
        return participantRepository.findById(id);
    }

    public Participant updateParticipant(String id, Participant participantDetails) {
        return participantRepository.findById(id)
                .map(participant -> {
                    participant.setName(participantDetails.getName());
                    participant.setSelectedItems(participantDetails.getSelectedItems());
                    return participantRepository.save(participant);
                })
                .orElseThrow(() -> new RuntimeException("Participant not found"));
    }

    public void deleteParticipant(String id) {
        participantRepository.deleteById(id);
    }
}
