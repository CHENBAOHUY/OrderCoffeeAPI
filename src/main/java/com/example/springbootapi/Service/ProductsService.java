package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Categories;
import com.example.springbootapi.Entity.Products;
import com.example.springbootapi.repository.CategoriesRepository;
import com.example.springbootapi.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductsService {
    @Autowired
    private ProductsRepository productsRepository;
    @Autowired
    private CategoriesRepository categoriesRepository;

    public List<Products> getAllProducts() {
        return productsRepository.findAll();
    }

    public Optional<Products> getProductById(Integer id) {
        return productsRepository.findById(id);
    }

    public List<Products> getProductsByCategory(Integer categoryId) {
        return productsRepository.findByCategoriesId(categoryId);
    }

    public Products createProduct(Products products) {
        Categories categories = categoriesRepository.findById(products.getCategories().getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        products.setCategories(categories);
        return productsRepository.save(products);
    }


    public Products updateProduct(Integer id, Products updatedProduct) {
        return productsRepository.findById(id).map(product -> {
            product.setName(updatedProduct.getName());
            product.setPrice(updatedProduct.getPrice());
            product.setCategories(updatedProduct.getCategories());
            return productsRepository.save(product);
        }).orElse(null);
    }

    public void deleteProduct(Integer id) {
        productsRepository.deleteById(id);
    }
}
