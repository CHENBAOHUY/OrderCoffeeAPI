package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.Users;
import com.example.springbootapi.Service.UsersService;
import com.example.springbootapi.dto.*;
import com.example.springbootapi.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    @Autowired
    private UsersService usersService;

    @Autowired
    private UserRepository usersRepository;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<UserProfileDTO> getCurrentUser(Authentication authentication) {
        logger.info("Fetching profile for user: {}", authentication.getName());
        UserProfileDTO userProfile = usersService.getUserProfile(authentication);
        logger.debug("User profile response: {}", userProfile);
        return ResponseEntity.ok(userProfile);
    }

    @PostMapping("/register/initiate")
    public ResponseEntity<?> initiateRegistration(@Valid @RequestBody UsersDTO usersDTO) {
        try {
            String message = usersService.initiateRegistration(usersDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/register/complete")
    public ResponseEntity<?> completeRegistration(@Valid @RequestBody UsersDTO usersDTO, @RequestParam String otp) {
        try {
            Users user = usersService.completeRegistration(usersDTO, otp);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Đăng ký thành công!");
            response.put("users", new UserDto(user.getId(), user.getName()));
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
            return ResponseEntity.status(401).body(createErrorResponse(e.getMessage()));
        }
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

    @PutMapping("/password")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO, Authentication authentication) {
        try {
            usersService.changePassword(authentication, changePasswordDTO);
            return ResponseEntity.ok(createSuccessResponse("Đổi mật khẩu thành công!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
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
}