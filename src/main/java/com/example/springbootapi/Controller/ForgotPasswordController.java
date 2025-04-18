package com.example.springbootapi.Controller;

import com.example.springbootapi.Entity.Users;
import com.example.springbootapi.dto.ForgotPasswordDTO;
import com.example.springbootapi.dto.ResetPasswordDTO;
import com.example.springbootapi.repository.UserRepository;
import com.example.springbootapi.Service.EmailService;
import jakarta.validation.Valid;
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
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {
        // Kiểm tra định dạng email
        if (!isValidEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email không đúng định dạng");
        }

        // Kiểm tra email và resetCode
        Optional<Users> userOptional = userRepository.findByEmailAndResetCode(request.getEmail(), request.getResetCode());
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Email hoặc mã OTP không hợp lệ");
        }

        Users user = userOptional.get();

        // Kiểm tra thời hạn của mã OTP
        if (user.getResetExpiry() == null || LocalDateTime.now().isAfter(user.getResetExpiry())) {
            return ResponseEntity.badRequest().body("Mã OTP đã hết hạn");
        }

        // Kiểm tra độ dài mật khẩu mới
        if (request.getNewPassword().length() < 8) {
            return ResponseEntity.badRequest().body("Mật khẩu mới phải có ít nhất 8 ký tự");
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetCode(null); // Xóa mã OTP sau khi sử dụng
        user.setResetExpiry(null); // Xóa thời hạn OTP
        userRepository.save(user);

        return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công");
    }

    // Phương thức kiểm tra định dạng email
    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w.-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,}$";
        return email != null && email.matches(emailRegex);
    }
}