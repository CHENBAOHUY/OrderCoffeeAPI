package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.PriceHistory;
import com.example.springbootapi.Service.PriceHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/price-history")
public class PriceHistoryController {

    @Autowired
    private PriceHistoryService priceHistoryService;

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updatePrice(
            @RequestParam Integer productId,
            @RequestParam BigDecimal newPrice) {
        priceHistoryService.updatePrice(productId, newPrice);
        return ResponseEntity.ok("Đã cập nhật giá sản phẩm");
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<PriceHistory>> getPriceHistory(@PathVariable Integer productId) {
        return ResponseEntity.ok(priceHistoryService.getPriceHistory(productId));
    }
}