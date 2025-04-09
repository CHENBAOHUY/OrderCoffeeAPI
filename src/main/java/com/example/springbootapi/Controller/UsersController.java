package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.Users;
import com.example.springbootapi.Service.UsersService;
import com.example.springbootapi.dto.*;
import com.example.springbootapi.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    @Autowired
    private UsersService usersService;

    @Autowired
    private UserRepository usersRepository;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    // Đăng ký tài khoản
    @PostMapping("/register")
    public ResponseEntity<?> registerUsers(@Valid @RequestBody UsersDTO usersDTO) {
        try {
            if (!usersDTO.getPassword().equals(usersDTO.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(createErrorResponse("Mật khẩu và xác nhận mật khẩu không khớp!"));
            }
            if (usersService.emailExists(usersDTO.getEmail())) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email đã tồn tại!"));
            }
            if (usersService.phoneExists(usersDTO.getPhone())) {
                return ResponseEntity.badRequest().body(createErrorResponse("Số điện thoại đã được sử dụng!"));
            }

            Users newUsers = usersService.registerUsers(usersDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đăng ký thành công!");
            response.put("users", new UserDto(newUsers.getId(), newUsers.getName()));
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> loginUsers(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseDTO response = usersService.loginUsers(loginRequest.getPhone(), loginRequest.getPassword());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Lỗi đăng nhập: " + e.getMessage());
            return ResponseEntity.status(401).body(createErrorResponse("Sai thông tin đăng nhập!"));
        }
    }

    // Lấy thông tin người dùng hiện tại
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<Users> getCurrentUser(Authentication authentication) {
        Integer userId = Integer.parseInt(authentication.getName());
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    // Xử lý lỗi validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    // Helper methods for consistent response
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
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