package com.pardos.pos.controller;

import com.pardos.pos.model.Order;
import com.pardos.pos.model.Sale;
import com.pardos.pos.service.OrderService;
import com.pardos.pos.service.SaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final SaleService saleService;

    public OrderController(OrderService orderService, SaleService saleService) {
        this.orderService = orderService;
        this.saleService = saleService;
    }

    @GetMapping
    public List<Order> all(@RequestParam(required = false) String status) {
        if (status != null) return orderService.findByStatus(status);
        return orderService.findAll();
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        Order created = orderService.create(order);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) String paymentMethod) {
        Order updated = orderService.updateStatus(id, status);
        if ("delivered".equalsIgnoreCase(status)) {
            String pm = paymentMethod != null ? paymentMethod : updated.getPaymentMethod();
            if (pm != null && !pm.isEmpty()) {
                Sale sale = new Sale();
                sale.setId(null);
                sale.setOrderId(updated.getId());
                sale.setOrderNumber(updated.getOrderNumber());
                sale.setTotal(updated.getTotal());
                sale.setPaymentMethod(pm);
                Sale completed = saleService.completeSale(sale);
                updated = orderService.findById(updated.getId()).orElse(updated);
            }
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Order> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
