package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Users;
import com.example.springbootapi.dto.ForgotPasswordDTO;
import com.example.springbootapi.dto.LoginResponseDTO;
import com.example.springbootapi.dto.ResetPasswordDTO;
import com.example.springbootapi.dto.UsersDTO;
import com.example.springbootapi.repository.UserRepository;
import com.example.springbootapi.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UsersService {

    @Autowired
    private UserRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    // Đăng ký tài khoản
    public Users registerUsers(UsersDTO usersDTO) {
        if (!EMAIL_PATTERN.matcher(usersDTO.getEmail()).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ!");
        }
        if (!PHONE_PATTERN.matcher(usersDTO.getPhone()).matches()) {
            throw new IllegalArgumentException("Số điện thoại phải có 10 số và bắt đầu bằng 0!");
        }
        if (!usersDTO.getPassword().equals(usersDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu và xác nhận mật khẩu không khớp!");
        }
        if (usersRepository.existsByEmail(usersDTO.getEmail())) {
            throw new IllegalArgumentException("Email đã được đăng ký!");
        }
        if (usersRepository.existsByPhone(usersDTO.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại đã được đăng ký!");
        }

        Users users = new Users();
        users.setName(usersDTO.getName());
        users.setEmail(usersDTO.getEmail());
        users.setPassword(passwordEncoder.encode(usersDTO.getPassword()));
        users.setPhone(usersDTO.getPhone());
        users.setRole(Users.Role.CUSTOMER);

        usersRepository.save(users);
        return users;
    }

    public LoginResponseDTO loginUsers(String phone, String password) {
        Users users = usersRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại!"));

        if (!passwordEncoder.matches(password, users.getPassword())) {
            throw new IllegalArgumentException("Sai mật khẩu!");
        }

        String token = jwtUtil.generateToken(users.getId().toString(), users.getRole().name());
        return new LoginResponseDTO(token, users.getPhone());
    }

    public boolean emailExists(String email) {
        return usersRepository.existsByEmail(email);
    }

    public boolean phoneExists(String phone) {
        return usersRepository.existsByPhone(phone);
    }

    // ✅ Gửi email đặt lại mật khẩu
    public void sendResetPasswordLink(ForgotPasswordDTO forgotPasswordDTO) {
        Optional<Users> usersOptional = usersRepository.findByEmail(forgotPasswordDTO.getEmail());
        if (usersOptional.isEmpty()) {
            throw new RuntimeException("Email không tồn tại trong hệ thống");
        }

        Users users = usersOptional.get();
        // Tạo OTP 6 chữ số
        String resetCode = String.format("%06d", new Random().nextInt(999999)); // Ví dụ: 123456
        users.setResetCode(resetCode);
        users.setResetExpiry(LocalDateTime.ofInstant(Instant.now().plusSeconds(15 * 60), ZoneId.systemDefault()));
        usersRepository.save(users);

        // Gửi OTP qua email
        emailService.sendEmail(users.getEmail(), "Đặt lại mật khẩu",
                "Mã OTP của bạn là: " + resetCode + ". Vui lòng nhập mã này trong ứng dụng để đặt lại mật khẩu.");
    }

    // ✅ Đặt lại mật khẩu
    @Transactional
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        Optional<Users> usersOptional = usersRepository.findByResetCode(resetPasswordDTO.getResetCode());
        if (usersOptional.isEmpty()) {
            throw new RuntimeException("Mã OTP không hợp lệ");
        }

        Users users = usersOptional.get();
        if (users.getResetExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }

        users.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        users.setResetCode(null);
        users.setResetExpiry(null);
        usersRepository.save(users);
    }
}