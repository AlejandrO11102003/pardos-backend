package com.pardos.pos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String status; // open, in_progress, resolved
    private String priority; // low, medium, high
    private String category; // Hardware, Software, Network, Other

    private Instant createdAt;
    private Instant resolvedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        if (this.status == null) this.status = "open";
        if (this.priority == null) this.priority = "medium";
        if (this.category == null) this.category = "Other";
    }
}
