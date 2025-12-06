package com.pardos.pos.service;

import com.pardos.pos.model.Order;
import com.pardos.pos.model.OrderItem;
import com.pardos.pos.repository.OrderRepository;
import com.pardos.pos.repository.ProductRepository;
import com.pardos.pos.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final TableService tableService;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository, TableService tableService, ProductRepository productRepository, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.tableService = tableService;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order create(Order order) {
        order.setId(null);
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setId(null);
                item.setOrder(order);
            }
        }
        long nextNumber = orderRepository.count() + 1;
        order.setOrderNumber(nextNumber);
        double subtotal = 0.0;
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                double price = item.getPrice();
                try {
                    if (item.getProductId() != null) {
                        long pid = Long.parseLong(item.getProductId());
                        productRepository.findById(pid).ifPresent(p -> {
                            item.setPrice(p.getPrice());
                        });
                    }
                } catch (NumberFormatException ignored) {}
                subtotal += item.getPrice() * item.getQuantity();
            }
        }
        double tax = Math.round(subtotal * 0.18 * 100.0) / 100.0;
        double total = Math.round((subtotal + tax) * 100.0) / 100.0;
        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setTotal(total);

        Order saved = orderRepository.save(order);

        if (saved.getTableNumber() != null) {
            Integer tn = saved.getTableNumber();
            tableService.findByNumber(tn).ifPresent(t -> {
                t.setStatus("occupied");
                t.setCurrentOrderId(String.valueOf(saved.getId()));
                tableService.save(t);
            });
            String msg = String.format("Nuevo pedido para la mesa %d (Orden #%d)", saved.getTableNumber(), saved.getOrderNumber());
            try { notificationService.add(msg, "info"); } catch (Exception ignored) {}
        }

        return saved;
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Order updateStatus(Long id, String status) {
        Order order = orderRepository.findById(id).orElseThrow();
        String prev = order.getStatus();
        order.setStatus(status);
        order.setUpdatedAt(java.time.Instant.now());
        Order saved = orderRepository.save(order);

        try {
            if ("delivered".equalsIgnoreCase(status)) {
                String msg = String.format("Pedido #%d entregado para la mesa %s", saved.getOrderNumber(), saved.getTableNumber() != null ? saved.getTableNumber() : "?");
                notificationService.add(msg, "success");
            } else if ("cancelled".equalsIgnoreCase(status)) {
                String msg = String.format("Pedido #%d cancelado (mesa %s)", saved.getOrderNumber(), saved.getTableNumber() != null ? saved.getTableNumber() : "?");
                notificationService.add(msg, "warning");
            } else if (!status.equalsIgnoreCase(prev)) {
                // generic update
                String msg = String.format("Pedido #%d cambió a estado %s", saved.getOrderNumber(), status);
                notificationService.add(msg, "info");
            }
        } catch (Exception ignored) {}

        return saved;
    }

    public Order cancelOrder(Long id) {
        Order cancelled = updateStatus(id, "cancelled");
        tableService.markTableCleaningByOrderId(String.valueOf(cancelled.getId()));
        return cancelled;
    }
}
