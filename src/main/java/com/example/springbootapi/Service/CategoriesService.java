package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Categories;
import com.example.springbootapi.repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

@Service
public class CategoriesService {
    private static final Logger logger = LoggerFactory.getLogger(CategoriesService.class);
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
        logger.info("Attempting to update category with id: {}", id);

        // Kiểm tra dữ liệu đầu vào
        if (id == null || updatedCategory == null) {
            logger.error("Invalid input: ID or updated category is null");
            throw new IllegalArgumentException("ID or updated category cannot be null");
        }

        return categoriesRepository.findById(id).map(category -> {
            // Cập nhật name nếu không null và hợp lệ
            if (updatedCategory.getName() != null && !updatedCategory.getName().isBlank()) {
                category.setName(updatedCategory.getName());
                logger.debug("Updated name to: {}", updatedCategory.getName());
            }

            // Cập nhật description nếu không null
            if (updatedCategory.getDescription() != null) {
                category.setDescription(updatedCategory.getDescription());
                logger.debug("Updated description to: {}", updatedCategory.getDescription());
            }

            // Cập nhật image nếu không null
            if (updatedCategory.getImage() != null) {
                category.setImage(updatedCategory.getImage());
                logger.debug("Updated image to: {}", updatedCategory.getImage());
            }

            // Lưu danh mục và tự động cập nhật updatedAt nhờ @PreUpdate
            Categories savedCategory = categoriesRepository.save(category);
            logger.info("Category updated successfully: {}", savedCategory.getName());
            return savedCategory;
        }).orElseThrow(() -> {
            logger.error("Category not found with id: {}", id);
            return new RuntimeException("Category not found with id: " + id);
        });
    }


    public void deleteCategory(Integer id) {
        categoriesRepository.deleteById(id);
    }
}
