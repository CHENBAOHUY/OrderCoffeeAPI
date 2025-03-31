package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Currencies;
import com.example.springbootapi.repository.CurrenciesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CurrenciesService {

    @Autowired
    private CurrenciesRepository currenciesRepository;

    public List<Currencies> getAllCurrencies() {
        return currenciesRepository.findAll();
    }

    public Optional<Currencies> getCurrencyById(Integer id) {
        return currenciesRepository.findById(id);
    }

    public Currencies saveCurrency(Currencies currency) {
        return currenciesRepository.save(currency);
    }

    public void deleteCurrency(Integer id) {
        currenciesRepository.deleteById(id);
    }

    public Currencies addCurrency(Currencies currency) {
        return currenciesRepository.save(currency);
    }
}
