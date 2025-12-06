package com.pardos.pos.controller;

import com.pardos.pos.model.Ticket;
import com.pardos.pos.repository.TicketRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    public List<Ticket> getAll() {
        return ticketRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Ticket> create(@RequestBody Ticket ticket) {
        Ticket saved = ticketRepository.save(ticket);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Ticket> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        ticket.setStatus(status);
        if ("resolved".equalsIgnoreCase(status)) {
            ticket.setResolvedAt(Instant.now());
        } else {
            ticket.setResolvedAt(null); // Reset if re-opened
        }
        return ResponseEntity.ok(ticketRepository.save(ticket));
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<Ticket> all = ticketRepository.findAll();
        
        // MTTR Calculation (in hours)
        double mttr = all.stream()
            .filter(t -> "resolved".equalsIgnoreCase(t.getStatus()) && t.getCreatedAt() != null && t.getResolvedAt() != null)
            .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes())
            .average()
            .orElse(0);
        
        // Common issues (by title/type - simplified for now by priority)
        Map<String, Long> byPriority = all.stream()
            .collect(Collectors.groupingBy(t -> t.getPriority() != null ? t.getPriority() : "unknown", Collectors.counting()));

        Map<String, Long> byStatus = all.stream()
            .collect(Collectors.groupingBy(t -> t.getStatus() != null ? t.getStatus() : "unknown", Collectors.counting()));

        // Common issues by category
        Map<String, Long> byCategory = all.stream()
            .collect(Collectors.groupingBy(t -> t.getCategory() != null ? t.getCategory() : "Other", Collectors.counting()));

        return Map.of(
            "mttrHours", Math.round((mttr / 60.0) * 100.0) / 100.0,
            "byPriority", byPriority,
            "byStatus", byStatus,
            "byCategory", byCategory
        );
    }
}
