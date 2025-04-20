package org.example.controller;

import jakarta.validation.Valid;
import org.example.model.Participant;
import org.example.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/participant")
@Tag(name = "Participant", description = "Endpoints for managing participants")
public class ParticipantController {
    @Autowired
    private ParticipantService participantService;

    @Operation(summary = "Create a new participant", description = "Creates a new participant with the provided details.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Participant created successfully")
    })
    @PostMapping
    public ResponseEntity<Participant> createParticipant(@Valid @RequestBody Participant participant) {
        participant.setId(UUID.randomUUID().toString());
        Participant savedParticipant = participantService.createParticipant(participant);
        return ResponseEntity.ok(savedParticipant);
    }

    @Operation(summary = "Get all participants", description = "Retrieves a list of all participants.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of participants returned successfully")
    })
    @GetMapping
    public ResponseEntity<List<Participant>> getAllParticipants() {
        return ResponseEntity.ok(participantService.getAllParticipants());
    }

    @Operation(summary = "Get participant by ID", description = "Retrieves a participant by their unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Participant found"),
        @ApiResponse(responseCode = "404", description = "Participant not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Participant> getParticipant(@PathVariable String id) {
        return participantService.getParticipantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update participant", description = "Updates an existing participant by their unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Participant updated successfully"),
        @ApiResponse(responseCode = "404", description = "Participant not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Participant> updateParticipant(@PathVariable String id, @Valid @RequestBody Participant participant) {
        try {
            Participant updated = participantService.updateParticipant(id, participant);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete participant", description = "Deletes a participant by their unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Participant deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Participant not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable String id) {
        participantService.deleteParticipant(id);
        return ResponseEntity.noContent().build();
    }
}
