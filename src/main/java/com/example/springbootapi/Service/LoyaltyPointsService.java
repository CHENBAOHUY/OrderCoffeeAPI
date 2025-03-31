package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.LoyaltyPoints;
import com.example.springbootapi.repository.LoyaltyPointsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LoyaltyPointsService {
    private final LoyaltyPointsRepository loyaltyPointsRepository;

    @Autowired
    public LoyaltyPointsService(LoyaltyPointsRepository loyaltyPointsRepository) {
        this.loyaltyPointsRepository = loyaltyPointsRepository;
    }

    public List<LoyaltyPoints> getAllLoyaltyPoints() {
        return loyaltyPointsRepository.findAll();
    }

    public Optional<LoyaltyPoints> getLoyaltyPointById(Integer id) {
        return loyaltyPointsRepository.findById(id);
    }

    public LoyaltyPoints addLoyaltyPoints(LoyaltyPoints loyaltyPoints) {
        return loyaltyPointsRepository.save(loyaltyPoints);
    }

    public void deleteLoyaltyPoints(Integer id) {
        loyaltyPointsRepository.deleteById(id);
    }
}
