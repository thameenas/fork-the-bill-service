package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Participant {
    @Id
    private String id;
    
    @NotBlank(message = "Name is required")
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "participant_selected_items", joinColumns = @JoinColumn(name = "participant_id"))
    @Column(name = "selected_item")
    private List<String> selectedItems = new ArrayList<>();
}
