package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.PriceHistory;
import com.example.springbootapi.repository.PriceHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PriceHistoryService {

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    public List<PriceHistory> getAllPriceHistory() {
        return priceHistoryRepository.findAll();
    }

    public Optional<PriceHistory> getPriceHistoryById(Integer id) {
        return priceHistoryRepository.findById(id);
    }

    public PriceHistory savePriceHistory(PriceHistory priceHistory) {
        return priceHistoryRepository.save(priceHistory);
    }


    public void deletePriceHistory(Integer id) {
        priceHistoryRepository.deleteById(id);
    }

    public PriceHistory addPriceHistory(PriceHistory priceHistory) {
        return priceHistoryRepository.save(priceHistory);
    }
}
