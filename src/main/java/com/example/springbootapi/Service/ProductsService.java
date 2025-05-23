package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Categories;
import com.example.springbootapi.Entity.PriceHistory;
import com.example.springbootapi.Entity.Products;
import com.example.springbootapi.repository.CategoriesRepository;
import com.example.springbootapi.repository.PriceHistoryRepository;
import com.example.springbootapi.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductsService {
    private static final Logger logger = LoggerFactory.getLogger(ProductsService.class);

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    public List<Products> getAllProducts() {
        return productsRepository.findAll();
    }

    public Optional<Products> getProductById(Integer productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        return productsRepository.findById(productId);
    }

    public List<Products> getProductsByCategory(Integer categoryId) {
        return productsRepository.findByCategoriesId(categoryId);
    }

    public Products createProduct(Products product) {
        if (product.getCategories() != null && product.getCategories().getId() != null) {
            Categories categories = categoriesRepository.findById(product.getCategories().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategories(categories);
        }
        return productsRepository.save(product);
    }

    public Products updateProduct(Integer id, Products updatedProduct) {
        logger.info("Attempting to update product with id: {}", id);

        if (id == null || updatedProduct == null) {
            logger.error("Invalid input: ID or updated product is null");
            throw new IllegalArgumentException("ID or updated product cannot be null");
        }

        return productsRepository.findById(id).map(product -> {
            BigDecimal oldPrice = product.getPrice();
            BigDecimal newPrice = updatedProduct.getPrice();

            // Cập nhật các trường khác
            if (updatedProduct.getName() != null && !updatedProduct.getName().isBlank()) {
                product.setName(updatedProduct.getName());
                logger.debug("Updated name to: {}", updatedProduct.getName());
            }
            if (updatedProduct.getDescription() != null) {
                product.setDescription(updatedProduct.getDescription());
                logger.debug("Updated description to: {}", updatedProduct.getDescription());
            }
            if (updatedProduct.getImage() != null) {
                product.setImage(updatedProduct.getImage());
                logger.debug("Updated image to: {}", updatedProduct.getImage());
            }
            if (updatedProduct.getStock() != null) {
                product.setStock(updatedProduct.getStock());
                logger.debug("Updated stock to: {}", updatedProduct.getStock());
            }
            if (updatedProduct.getCategories() != null && updatedProduct.getCategories().getId() != null) {
                Categories categories = categoriesRepository.findById(updatedProduct.getCategories().getId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                product.setCategories(categories);
                logger.debug("Updated category to: {}", categories.getName());
            }

            // Kiểm tra và lưu lịch sử giá nếu giá thay đổi
            if (newPrice != null) {
                if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    logger.error("Invalid price: Price must be greater than 0 for product id: {}", id);
                    throw new IllegalArgumentException("Price must be greater than 0");
                }
                if (!oldPrice.equals(newPrice)) {
                    try {
                        PriceHistory history = new PriceHistory();
                        history.setProductId(id);
                        history.setOldPrice(oldPrice);
                        history.setNewPrice(newPrice);
                        history.setChangedAt(LocalDateTime.now());
                        priceHistoryRepository.save(history);
                        logger.info("Saved price history for product id: {}", id);
                    } catch (Exception e) {
                        logger.error("Failed to save price history for product id: {}. Error: {}", id, e.getMessage());
                        throw new RuntimeException("Failed to save price history", e);
                    }
                }
                product.setPrice(newPrice);
                logger.debug("Updated price to: {}", newPrice);
            }

            Products savedProduct = productsRepository.save(product);
            logger.info("Product updated successfully: {}", savedProduct.getName());
            return savedProduct;
        }).orElseThrow(() -> {
            logger.error("Product not found with id: {}", id);
            return new RuntimeException("Product not found with id: " + id);
        });
    }

    public void deleteProduct(Integer id) {
        productsRepository.deleteById(id);
    }
}