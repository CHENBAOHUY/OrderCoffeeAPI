package com.example.springbootapi.Controller;

import com.example.springbootapi.dto.ResetPasswordRequest;
import com.example.springbootapi.Entity.Users;
import com.example.springbootapi.repository.UserRepository;
import com.example.springbootapi.Service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${frontend.reset-password-url}")
    private String frontendResetUrl;

    public ForgotPasswordController(UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // Gửi email reset mật khẩu
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ResetPasswordRequest request) {
        Optional<Users> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Email không tồn tại trong hệ thống");
        }

        Users user = userOptional.get();
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(30); // Token hết hạn sau 30 phút

        user.setResetCode(token);
        user.setResetExpiry(expiryTime);
        userRepository.save(user);

        String resetLink = frontendResetUrl + "?token=" + token;
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);

        return ResponseEntity.ok("Link đặt lại mật khẩu đã được gửi đến email của bạn");
    }

    // Đặt lại mật khẩu
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
        Optional<Users> userOptional = userRepository.findByResetCode(token);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Token không hợp lệ");
        }

        Users user = userOptional.get();

        if (user.getResetExpiry() == null || LocalDateTime.now().isAfter(user.getResetExpiry())) {
            return ResponseEntity.badRequest().body("Token đã hết hạn");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetCode(null);
        user.setResetExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công");
    }
}
