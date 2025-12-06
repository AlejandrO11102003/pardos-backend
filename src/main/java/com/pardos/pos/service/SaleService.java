package com.pardos.pos.service;

import com.pardos.pos.model.Order;
import com.pardos.pos.model.Sale;
import com.pardos.pos.repository.OrderRepository;
import com.pardos.pos.repository.SaleRepository;
import com.pardos.pos.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaleService {
    private final SaleRepository saleRepository;
    private final OrderRepository orderRepository;
    private final TableService tableService;
    private final NotificationService notificationService;

    public SaleService(SaleRepository saleRepository, OrderRepository orderRepository, TableService tableService, NotificationService notificationService) {
        this.saleRepository = saleRepository;
        this.orderRepository = orderRepository;
        this.tableService = tableService;
        this.notificationService = notificationService;
    }

    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    public Sale completeSale(Sale sale) {
        // mark order delivered
        Order order = orderRepository.findById(sale.getOrderId()).orElseThrow();
        order.setStatus("delivered");
        orderRepository.save(order);
        tableService.markTableCleaningByOrderId(String.valueOf(order.getId()));
        try {
            String msg = String.format("Pedido #%d entregado para la mesa %s", order.getOrderNumber(), order.getTableNumber() != null ? order.getTableNumber() : "?");
            notificationService.add(msg, "success");
            // notify about table cleaning
            if (order.getTableNumber() != null) {
                String m = String.format("Mesa %d en limpieza", order.getTableNumber());
                notificationService.add(m, "info");
            }
        } catch (Exception ignored) {}
        return saleRepository.save(sale);
    }
}
