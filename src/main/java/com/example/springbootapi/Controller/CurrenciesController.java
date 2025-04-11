package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.Currencies;
import com.example.springbootapi.Service.CurrenciesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/currencies")
public class CurrenciesController {

    @Autowired
    private CurrenciesService currenciesService;

    @GetMapping
    public List<Currencies> getAllCurrencies() {
        return currenciesService.getAllCurrencies();
    }

    @GetMapping("/{id}")
    public Optional<Currencies> getCurrencyById(@PathVariable Integer id) {
        return currenciesService.getCurrencyById(id);
    }

    @PostMapping
    public Currencies addCurrency(@RequestBody Currencies currency) {
        return currenciesService.addCurrency(currency);
    }

    @DeleteMapping("/{id}")
    public void deleteCurrency(@PathVariable Integer id) {
        currenciesService.deleteCurrency(id);
    }
}