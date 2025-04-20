package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    private String id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Amount is required")
    private Double amount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_assignees", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "assignee")
    private List<String> assignedTo = new ArrayList<>();
}
