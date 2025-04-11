package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.LoyaltyPoints;
import com.example.springbootapi.Entity.SystemConfig;
import com.example.springbootapi.Entity.Users;
import com.example.springbootapi.dto.LoyaltyPointsDTO;
import com.example.springbootapi.repository.LoyaltyPointsRepository;
import com.example.springbootapi.repository.SystemConfigRepository;
import com.example.springbootapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoyaltyPointsService {

    @Autowired
    private LoyaltyPointsRepository loyaltyPointsRepository;

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Autowired
    private UserRepository usersRepository;

    @Transactional
    public void addPoints(Integer userId, BigDecimal paymentAmount, String currencyCode) {
        SystemConfig config = systemConfigRepository.findActiveConfigByCurrencyCode(currencyCode, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cấu hình cho " + currencyCode));

        BigDecimal points = paymentAmount.divide(config.getFromValuePrice(), 2, BigDecimal.ROUND_HALF_UP)
                .multiply(config.getToValuePoint());

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        LoyaltyPoints loyaltyPoints = new LoyaltyPoints();
        loyaltyPoints.setUser(user);
        loyaltyPoints.setPoints(points.intValue());
        loyaltyPoints.setSource("Order");
        loyaltyPoints.setExpiresAt(LocalDateTime.now().plusYears(1));
        loyaltyPointsRepository.save(loyaltyPoints);

        user.setPoints(user.getPoints() + points.intValue());
        usersRepository.save(user);
    }

    public List<LoyaltyPointsDTO> getPointsHistory(Integer userId) {
        return loyaltyPointsRepository.findByUserId(userId)
                .stream()
                .map(loyaltyPoints -> new LoyaltyPointsDTO(loyaltyPoints)) // Sửa thành lambda
                .collect(Collectors.toList());
    }
}