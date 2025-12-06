package com.pardos.pos.service;

import com.pardos.pos.model.TableEntity;
import com.pardos.pos.repository.TableRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import jakarta.annotation.PreDestroy;
import com.pardos.pos.service.NotificationService;

@Service
public class TableService {
    private final TableRepository tableRepository;
    private final NotificationService notificationService;

    public TableService(TableRepository tableRepository, NotificationService notificationService) {
        this.tableRepository = tableRepository;
        this.notificationService = notificationService;
    }

    public List<TableEntity> findAll() {
        return tableRepository.findAll();
    }

    public Optional<TableEntity> findById(Long id) {
        return tableRepository.findById(id);
    }

    public TableEntity save(TableEntity table) {
        // detect status change
        try {
            if (table.getId() != null) {
                Optional<TableEntity> existing = tableRepository.findById(table.getId());
                if (existing.isPresent()) {
                    String prev = existing.get().getStatus();
                    String next = table.getStatus();
                    if (next != null && !next.equals(prev)) {
                        String msg = String.format("Mesa %d ahora %s", table.getNumber(), next);
                        try { notificationService.add(msg, "info"); } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}
        return tableRepository.save(table);
    }

    public Optional<TableEntity> findByNumber(Integer number) {
        return tableRepository.findByNumber(number);
    }
    public void markTableCleaningByNumber(Integer tableNumber) {
        if (tableNumber == null) return;
        Optional<TableEntity> maybe = tableRepository.findByNumber(tableNumber);
        maybe.ifPresent(t -> {
            t.setStatus("cleaning");
            t.setCurrentOrderId(null);
            save(t);
        });
    }

    public void markTableCleaningByOrderId(String orderId) {
        if (orderId == null) return;
        Optional<TableEntity> maybe = tableRepository.findByCurrentOrderId(orderId);
        maybe.ifPresent(t -> {
            t.setStatus("cleaning");
            t.setCurrentOrderId(null);
            save(t);
        });
    }

    @PreDestroy
    public void onDestroy() {
    }
}
