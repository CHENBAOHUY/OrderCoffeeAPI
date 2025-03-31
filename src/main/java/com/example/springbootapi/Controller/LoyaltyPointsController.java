package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.LoyaltyPoints;
import com.example.springbootapi.Service.LoyaltyPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/loyalty-points")
public class LoyaltyPointsController {

    @Autowired
    private LoyaltyPointsService loyaltyPointsService;

    @GetMapping
    public List<LoyaltyPoints> getAllLoyaltyPoints() {
        return loyaltyPointsService.getAllLoyaltyPoints();
    }

    @GetMapping("/{id}")
    public Optional<LoyaltyPoints> getLoyaltyPointById(@PathVariable Integer id) {
        return loyaltyPointsService.getLoyaltyPointById(id);
    }

    @PostMapping
    public LoyaltyPoints addLoyaltyPoints(@RequestBody LoyaltyPoints loyaltyPoints) {
        return loyaltyPointsService.addLoyaltyPoints(loyaltyPoints);
    }

    @DeleteMapping("/{id}")
    public void deleteLoyaltyPoints(@PathVariable Integer id) {
        loyaltyPointsService.deleteLoyaltyPoints(id);
    }
}
