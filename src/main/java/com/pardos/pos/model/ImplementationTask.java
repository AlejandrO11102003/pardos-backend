package com.pardos.pos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "implementation_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImplementationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private boolean completed;
    
    @Column(name = "task_order")
    private int order;
}
