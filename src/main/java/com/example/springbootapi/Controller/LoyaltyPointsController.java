package com.example.springbootapi.Controller;

import com.example.springbootapi.dto.LoyaltyPointsDTO;
import com.example.springbootapi.Service.LoyaltyPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/loyalty-points")
public class LoyaltyPointsController {

    @Autowired
    private LoyaltyPointsService loyaltyPointsService;

    @PostMapping("/add")
    public ResponseEntity<String> addPoints(
            @RequestParam Integer userId,
            @RequestParam BigDecimal paymentAmount,
            @RequestParam String currencyCode) {
        loyaltyPointsService.addPoints(userId, paymentAmount, currencyCode);
        return ResponseEntity.ok("Đã cộng " + paymentAmount + " " + currencyCode + " thành điểm thưởng");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<LoyaltyPointsDTO>> getPointsHistory(@PathVariable Integer userId) {
        return ResponseEntity.ok(loyaltyPointsService.getPointsHistory(userId));
    }
}