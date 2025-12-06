package com.pardos.pos.controller;

import com.pardos.pos.model.Sale;
import com.pardos.pos.model.Product;
import com.pardos.pos.model.Order;
import com.pardos.pos.repository.SaleRepository;
import com.pardos.pos.repository.ProductRepository;
import com.pardos.pos.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    @Autowired
    private SaleRepository saleRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;

    // devuelve ventas agrupadas por fecha
    @GetMapping("/sales-by-day")
    public Map<String, Double> getSalesByDay() {
        List<Sale> sales = saleRepository.findAll();
        Map<String, Double> result = new HashMap<>();
        for (Sale sale : sales) {
            LocalDate date = sale.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate();
            String key = date.toString();
            result.put(key, result.getOrDefault(key, 0.0) + sale.getTotal());
        }
        return result;
    }

    // devuelve productos mas vendidos x cant
    @GetMapping("/top-products")
    public List<Map<String, Object>> getTopProducts() {
        // calcular cantidades reales a partir de ordenes entregadas
        List<Order> delivered = orderRepository.findByStatus("delivered");
        Map<String, Map<String, Object>> agg = new HashMap<>();

        for (Order o : delivered) {
            if (o.getItems() == null) continue;
            o.getItems().forEach(item -> {
                String pid = item.getProductId();
                String name = item.getProductName();
                int qty = item.getQuantity();
                Map<String, Object> entry = agg.get(pid);
                if (entry == null) {
                    entry = new HashMap<>();
                    entry.put("productId", pid);
                    entry.put("name", name);
                    entry.put("sold", 0);
                    agg.put(pid, entry);
                }
                int prev = (int) entry.get("sold");
                entry.put("sold", prev + qty);
            });
        }

        List<Map<String, Object>> list = new ArrayList<>(agg.values());
        list.sort((a, b) -> Integer.compare((int)b.get("sold"), (int)a.get("sold")));
        return list.stream().limit(10).collect(Collectors.toList());
    }
    @GetMapping("/governance")
    public Map<String, Object> getGovernanceMetrics() {
        List<Order> all = orderRepository.findAll();
        long total = all.size();
        long cancelled = all.stream().filter(o -> "cancelled".equalsIgnoreCase(o.getStatus())).count();

        double errorRate = total > 0 ? (double) cancelled / total * 100 : 0;
        double avgAttentionTime = all.stream()
            .filter(o -> "delivered".equalsIgnoreCase(o.getStatus()) && o.getCreatedAt() != null && o.getUpdatedAt() != null)
            .mapToLong(o -> java.time.Duration.between(o.getCreatedAt(), o.getUpdatedAt()).toSeconds())
            .average()
            .orElse(0);

        double avgMinutes = avgAttentionTime / 60.0;

        // Disponibilidad, simulado, se necesita servidor 
        double availability = 99.9;
        return Map.of(
            "availability", availability,
            "attentionTime", Math.round(avgMinutes * 100.0) / 100.0,
            "errorRate", Math.round(errorRate * 100.0) / 100.0
        );
    }
}
