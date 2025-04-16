package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.PriceHistory;
import com.example.springbootapi.Entity.Products;
import com.example.springbootapi.repository.PriceHistoryRepository;
import com.example.springbootapi.repository.ProductsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PriceHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(PriceHistoryService.class);

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Transactional
    public void updatePrice(Integer productId, BigDecimal newPrice) {
        if (productId == null || productId <= 0) {
            logger.error("Invalid product ID: {}", productId);
            throw new IllegalArgumentException("Product ID must be positive");
        }
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Invalid price: {} for product ID: {}", newPrice, productId);
            throw new IllegalArgumentException("Price must be positive");
        }

        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID: {}", productId);
                    return new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId);
                });

        BigDecimal oldPrice = product.getPrice();
        if (!oldPrice.equals(newPrice)) {
            try {
                PriceHistory history = new PriceHistory();
                history.setProductId(productId);
                history.setOldPrice(oldPrice);
                history.setNewPrice(newPrice);
                history.setChangedAt(LocalDateTime.now());
                priceHistoryRepository.save(history);
                logger.info("Saved price history for product ID: {}, oldPrice: {}, newPrice: {}",
                        productId, oldPrice, newPrice);
            } catch (Exception e) {
                logger.error("Failed to save price history for product ID: {}. Error: {}", productId, e.getMessage());
                throw new RuntimeException("Không thể lưu lịch sử giá", e);
            }

            product.setPrice(newPrice);
            productsRepository.save(product);
            logger.info("Updated price for product ID: {} to {}", productId, newPrice);
        } else {
            logger.info("Price unchanged for product ID: {}, current price: {}", productId, oldPrice);
        }
    }

    public List<PriceHistory> getPriceHistory(Integer productId) {
        if (productId == null || productId <= 0) {
            logger.error("Invalid product ID: {}", productId);
            throw new IllegalArgumentException("Product ID must be positive");
        }
        List<PriceHistory> history = priceHistoryRepository.findByProductId(productId);
        if (history.isEmpty()) {
            logger.warn("No price history found for product ID: {}", productId);
        } else {
            logger.info("Retrieved {} price history records for product ID: {}", history.size(), productId);
        }
        return history;
    }
    public List<PriceHistory> getAllPriceHistory() {
        return priceHistoryRepository.findAll();
    }
}