package com.pardos.pos.controller;

import com.pardos.pos.model.TableEntity;
import com.pardos.pos.service.TableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "http://localhost:5173")
public class TableController {
    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public List<TableEntity> all() {
        return tableService.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TableEntity> update(@PathVariable Long id, @RequestBody TableEntity table) {
        table.setId(id);
        return ResponseEntity.ok(tableService.save(table));
    }
}
