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
public class Item {
    @Id
    private String id;
    @jakarta.validation.constraints.NotBlank(message = "Name is required")
    private String name;
    @jakarta.validation.constraints.NotNull(message = "Amount is required")
    private Double amount;

    @ElementCollection
    @jakarta.validation.constraints.NotNull(message = "assignedTo is required")
    @jakarta.validation.constraints.Size(min = 1, message = "At least one assignee required")
    private List<String> assignedTo;
}
