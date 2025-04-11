package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.Categories;
import com.example.springbootapi.Service.CategoriesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoriesController {

    @Autowired
    private CategoriesService categoriesService;

    @GetMapping
    @PermitAll
    public ResponseEntity<?> getAllCategories() {
        List<Categories> categories = categoriesService.getAllCategories();
        if (categories.isEmpty()) {
            return ResponseEntity.status(404).body(createErrorResponse("Không có danh mục nào!"));
        }
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> getCategoryById(@PathVariable Integer id) {
        Optional<Categories> category = categoriesService.getCategoryById(id);
        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        } else {
            return ResponseEntity.status(404).body(createErrorResponse("Danh mục không tồn tại!"));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCategory(@Valid @RequestBody Categories category) {
        try {
            Categories newCategory = categoriesService.createCategory(category);
            return ResponseEntity.status(201).body(newCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @Valid @RequestBody Categories updatedCategory) {
        Categories category = categoriesService.updateCategory(id, updatedCategory);
        return category != null
                ? ResponseEntity.ok(category)
                : ResponseEntity.status(404).body(createErrorResponse("Danh mục không tồn tại!"));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        Optional<Categories> category = categoriesService.getCategoryById(id);
        if (category.isPresent()) {
            categoriesService.deleteCategory(id);
            return ResponseEntity.ok(createSuccessResponse("Danh mục đã được xóa!"));
        } else {
            return ResponseEntity.status(404).body(createErrorResponse("Danh mục không tồn tại!"));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Lỗi");
        response.put("message", message);
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return response;
    }
}