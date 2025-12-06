package com.pardos.pos.controller;

import com.pardos.pos.model.Sale;
import com.pardos.pos.service.SaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "http://localhost:5173")
public class SaleController {
    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public List<Sale> all() {
        return saleService.findAll();
    }

    @PostMapping
    public ResponseEntity<Sale> complete(@RequestBody Sale sale) {
        // ensure DB generates id
        sale.setId(null);
        return ResponseEntity.ok(saleService.completeSale(sale));
    }
}
