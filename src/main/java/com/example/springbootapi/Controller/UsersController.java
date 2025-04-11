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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<Users> getCurrentUser(Authentication authentication) {
        Integer userId = Integer.parseInt(authentication.getName());
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryDTO>> getAllUsersSummary() {
        List<Users> users = usersService.getAllActiveUsers();
        List<UserSummaryDTO> userSummaries = users.stream()
                .map(user -> new UserSummaryDTO(user.getId(), user.getName(), user.getPoints()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(userSummaries);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserDetails(@PathVariable Integer id) {
        Optional<Users> userOptional = usersService.getActiveUserById(id);
        if (userOptional.isPresent()) {
            Users user = userOptional.get();
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("name", user.getName());
            userDetails.put("email", user.getEmail());
            userDetails.put("phone", user.getPhone());
            userDetails.put("points", user.getPoints());
            return ResponseEntity.ok(userDetails);
        } else {
            return ResponseEntity.status(404).body(createErrorResponse("Không tìm thấy người dùng với ID: " + id));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        Optional<Users> userOptional = usersService.getActiveUserById(id);
        if (userOptional.isPresent()) {
            usersService.deleteUser(id);
            return ResponseEntity.ok(createSuccessResponse("Người dùng ID: " + id + " đã được xóa thành công."));
        } else {
            return ResponseEntity.status(404).body(createErrorResponse("Không tìm thấy người dùng với ID: " + id));
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserUpdateDTO updateDTO, Authentication authentication) {
        try {
            Users updatedUser = usersService.updateUserProfile(authentication, updateDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cập nhật thông tin thành công");
            response.put("user", new UserSummaryDTO(updatedUser.getId(), updatedUser.getName(), updatedUser.getPoints()));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> deleteProfile(Authentication authentication) {
        try {
            usersService.deleteUserAccount(authentication);
            return ResponseEntity.ok(createSuccessResponse("Tài khoản đã được xóa thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

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

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return response;
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Lỗi");
        response.put("message", message);
        return response;
    }
}