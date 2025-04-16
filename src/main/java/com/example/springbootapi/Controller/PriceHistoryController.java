package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.PriceHistory;
import com.example.springbootapi.Service.PriceHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/price-history")
public class PriceHistoryController {

    @Autowired
    private PriceHistoryService priceHistoryService;

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePrice(
            @RequestParam Integer productId,
            @RequestParam BigDecimal newPrice) {
        try {
            priceHistoryService.updatePrice(productId, newPrice);
            return ResponseEntity.ok(createSuccessResponse("Cập nhật giá sản phẩm thành công cho ID: " + productId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Lỗi server khi cập nhật giá"));
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getPriceHistory(@PathVariable Integer productId) {
        try {
            List<PriceHistory> history = priceHistoryService.getPriceHistory(productId);
            if (history.isEmpty()) {
                return ResponseEntity.status(404).body(createErrorResponse("Không tìm thấy lịch sử giá cho sản phẩm ID: " + productId));
            }
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPriceHistory() {
        try {
            List<PriceHistory> allHistory = priceHistoryService.getAllPriceHistory();
            if (allHistory.isEmpty()) {
                return ResponseEntity.status(404).body(createErrorResponse("Không tìm thấy lịch sử giá."));
            }
            return ResponseEntity.ok(allHistory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Lỗi");
        response.put("message", message);
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return response;
    }
}