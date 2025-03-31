package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Categories;
import com.example.springbootapi.repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriesService {
    @Autowired
    private CategoriesRepository categoriesRepository;

    public List<Categories> getAllCategories() {
        return categoriesRepository.findAll();
    }

    public Optional<Categories> getCategoryById(Integer id) {
        return categoriesRepository.findById(id);
    }

    public Categories createCategory(Categories category) {
        return categoriesRepository.save(category);
    }

    public Categories updateCategory(Integer id, Categories updatedCategory) {
        return categoriesRepository.findById(id).map(category -> {
            category.setName(updatedCategory.getName());
            return categoriesRepository.save(category);
        }).orElse(null);
    }

    public void deleteCategory(Integer id) {
        categoriesRepository.deleteById(id);
    }
}
