package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Participant {
    @Id
    private String id;
    @jakarta.validation.constraints.NotBlank(message = "Name is required")
    private String name;

    @ElementCollection
    @jakarta.validation.constraints.NotNull(message = "selectedItems is required")
    @jakarta.validation.constraints.Size(min = 1, message = "At least one selected item required")
    private List<String> selectedItems;
}
