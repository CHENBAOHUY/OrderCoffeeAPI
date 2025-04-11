package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.Products;
import com.example.springbootapi.Service.ProductsService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductsController {

    @Autowired
    private ProductsService productsService;

    @GetMapping
    @PermitAll
    public ResponseEntity<?> getAllProducts() {
        List<Products> products = productsService.getAllProducts();
        if (products.isEmpty()) {
            return ResponseEntity.status(404).body(createErrorResponse("Không có sản phẩm nào!"));
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> getProductById(@PathVariable Integer id) {
        Optional<Products> product = productsService.getProductById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            return ResponseEntity.status(404).body(createErrorResponse("Không tìm thấy sản phẩm với ID: " + id));
        }
    }

    @GetMapping("/category/{categoryId}")
    @PermitAll
    public ResponseEntity<?> getProductsByCategory(@PathVariable Integer categoryId) {
        List<Products> products = productsService.getProductsByCategory(categoryId);
        if (products.isEmpty()) {
            return ResponseEntity.status(404).body(createErrorResponse("Không có sản phẩm nào trong danh mục ID: " + categoryId));
        }
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody Products product) {
        try {
            Products newProduct = productsService.createProduct(product);
            return ResponseEntity.ok(newProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @Valid @RequestBody Products updatedProduct) {
        Products product = productsService.updateProduct(id, updatedProduct);
        return product != null
                ? ResponseEntity.ok(product)
                : ResponseEntity.status(404).body(createErrorResponse("Không tìm thấy sản phẩm để cập nhật với ID: " + id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        Optional<Products> product = productsService.getProductById(id);
        if (product.isPresent()) {
            productsService.deleteProduct(id);
            return ResponseEntity.ok(createSuccessResponse("Sản phẩm ID: " + id + " đã được xóa thành công."));
        } else {
            return ResponseEntity.status(404).body(createErrorResponse("Không tìm thấy sản phẩm với ID: " + id));
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