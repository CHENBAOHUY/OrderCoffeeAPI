package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.Users;
import com.example.springbootapi.dto.ForgotPasswordDTO;
import com.example.springbootapi.dto.ResetPasswordDTO;
import com.example.springbootapi.repository.UserRepository;
import com.example.springbootapi.Service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

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
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordDTO request) {
        Optional<Users> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Email không tồn tại trong hệ thống");
        }

        Users user = userOptional.get();
        String otp = String.format("%06d", new Random().nextInt(999999)); // Tạo OTP 6 chữ số
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15); // Hết hạn sau 15 phút

        user.setResetCode(otp);
        user.setResetExpiry(expiryTime);
        userRepository.save(user);

        // Gửi OTP qua email
        emailService.sendResetPasswordEmail(user.getEmail(), otp);

        return ResponseEntity.ok("Mã OTP đã được gửi đến email của bạn");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDTO request) {
        Optional<Users> userOptional = userRepository.findByResetCode(request.getResetCode());
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Mã OTP không hợp lệ");
        }

        Users user = userOptional.get();
        if (user.getResetExpiry() == null || LocalDateTime.now().isAfter(user.getResetExpiry())) {
            return ResponseEntity.badRequest().body("Mã OTP đã hết hạn");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetCode(null);
        user.setResetExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công");
    }
}