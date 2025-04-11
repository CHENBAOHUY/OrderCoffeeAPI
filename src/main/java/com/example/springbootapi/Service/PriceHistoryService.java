package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.PriceHistory;
import com.example.springbootapi.Entity.Products;
import com.example.springbootapi.repository.PriceHistoryRepository;
import com.example.springbootapi.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PriceHistoryService {

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Transactional
    public void updatePrice(Integer productId, BigDecimal newPrice) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        PriceHistory history = new PriceHistory();
        history.setProductId(productId);
        history.setOldPrice(product.getPrice()); // Dòng 30: Giờ OK vì cả hai đều là BigDecimal
        history.setNewPrice(newPrice);
        priceHistoryRepository.save(history);

        product.setPrice(newPrice); // Giờ OK vì cả hai đều là BigDecimal
        productsRepository.save(product);
    }

    public List<PriceHistory> getPriceHistory(Integer productId) {
        return priceHistoryRepository.findByProductId(productId);
    }
}