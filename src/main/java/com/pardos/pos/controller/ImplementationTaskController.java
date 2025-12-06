package com.pardos.pos.controller;

import com.pardos.pos.model.ImplementationTask;
import com.pardos.pos.repository.ImplementationTaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/implementation-tasks")
public class ImplementationTaskController {
    private final ImplementationTaskRepository repository;

    public ImplementationTaskController(ImplementationTaskRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ImplementationTask> getAll() {
        return repository.findAllByOrderByOrderAsc();
    }

    @PostMapping
    public ResponseEntity<ImplementationTask> create(@RequestBody ImplementationTask task) {
        // Auto-assign order if not provided (simple logic: put at end)
        if (task.getOrder() == 0) {
            long count = repository.count();
            task.setOrder((int) count + 1);
        }
        return ResponseEntity.ok(repository.save(task));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<ImplementationTask> toggle(@PathVariable Long id) {
        ImplementationTask task = repository.findById(id).orElseThrow();
        task.setCompleted(!task.isCompleted());
        return ResponseEntity.ok(repository.save(task));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
