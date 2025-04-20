package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
    @Id
    private String id;
    private String title;
    private String creatorName;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants;

    private double tax;
    private double serviceCharge;
    private String status;
    private LocalDateTime createdAt;
}
