package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.PriceHistory;
import com.example.springbootapi.Service.PriceHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/price-history")
public class PriceHistoryController {

    @Autowired
    private PriceHistoryService priceHistoryService;

    @GetMapping
    public List<PriceHistory> getAllPriceHistory() {
        return priceHistoryService.getAllPriceHistory();
    }

    @GetMapping("/{id}")
    public Optional<PriceHistory> getPriceHistoryById(@PathVariable Integer id) {
        return priceHistoryService.getPriceHistoryById(id);
    }

    @PostMapping
    public PriceHistory addPriceHistory(@RequestBody PriceHistory priceHistory) {
        return priceHistoryService.addPriceHistory(priceHistory);
    }

    @DeleteMapping("/{id}")
    public void deletePriceHistory(@PathVariable Integer id) {
        priceHistoryService.deletePriceHistory(id);
    }
}
