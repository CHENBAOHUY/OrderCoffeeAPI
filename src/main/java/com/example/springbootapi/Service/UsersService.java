package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Users;
import com.example.springbootapi.dto.*;
import com.example.springbootapi.repository.UserRepository;
import com.example.springbootapi.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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

    // Giữ nguyên phương thức này
    public Integer getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User is not authenticated");
        }
        String userIdStr = authentication.getName();
        try {
            return Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to extract user ID from authentication");
        }
    }

    //  initiateRegistration
    @Transactional
    public String initiateRegistration(UsersDTO usersDTO) {
        if (!EMAIL_PATTERN.matcher(usersDTO.getEmail()).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ!");
        }
        if (!PHONE_PATTERN.matcher(usersDTO.getPhone()).matches()) {
            throw new IllegalArgumentException("Số điện thoại phải có 10 số và bắt đầu bằng 0!");
        }
        if (!usersDTO.getPassword().equals(usersDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu và xác nhận mật khẩu không khớp!");
        }

        Optional<Users> existingEmailUser = usersRepository.findByEmail(usersDTO.getEmail());
        Optional<Users> existingPhoneUser = usersRepository.findByPhone(usersDTO.getPhone());

        // Kiểm tra email
        if (existingEmailUser.isPresent()) {
            Users emailUser = existingEmailUser.get();
            if (!emailUser.isDeleted()) {
                throw new IllegalArgumentException("Email đã được đăng ký!");
            }
            // Nếu email trùng nhưng phone không trùng
            if (!existingPhoneUser.isPresent() || !existingPhoneUser.get().getPhone().equals(emailUser.getPhone())) {
                throw new IllegalArgumentException("Email đã tồn tại trong hệ thống (tài khoản bị xóa mềm). Vui lòng dùng email khác hoặc liên hệ hỗ trợ!");
            }
        }

        // Kiểm tra phone
        if (existingPhoneUser.isPresent()) {
            Users phoneUser = existingPhoneUser.get();
            if (!phoneUser.isDeleted()) {
                throw new IllegalArgumentException("Số điện thoại đã được đăng ký!");
            }
            // Nếu phone trùng nhưng email không trùng
            if (!existingEmailUser.isPresent() || !existingEmailUser.get().getEmail().equals(phoneUser.getEmail())) {
                throw new IllegalArgumentException("Số điện thoại đã tồn tại trong hệ thống (tài khoản bị xóa mềm). Vui lòng dùng số khác hoặc liên hệ hỗ trợ!");
            }
        }

        // Nếu cả email và phone đều trùng với cùng một tài khoản bị xóa mềm
        if (existingEmailUser.isPresent() && existingPhoneUser.isPresent() &&
                existingEmailUser.get().getId().equals(existingPhoneUser.get().getId())) {
            Users user = existingEmailUser.get();
            String otp = String.format("%06d", new Random().nextInt(999999));
            user.setResetCode(otp);
            user.setResetExpiry(LocalDateTime.now().plusMinutes(15));
            usersRepository.save(user);
            emailService.sendEmail(user.getEmail(), "Xác minh đăng ký tài khoản",
                    "Mã OTP của bạn là: " + otp + ". Vui lòng nhập mã này để tiếp tục đăng ký.");
            return "OTP đã được gửi đến email của bạn để xác minh.";
        }

        // Nếu không trùng, tạo tài khoản mới
        Users users = new Users();
        users.setName(usersDTO.getName());
        users.setEmail(usersDTO.getEmail());
        users.setPassword(passwordEncoder.encode(usersDTO.getPassword()));
        users.setPhone(usersDTO.getPhone());
        users.setRole(Users.Role.CUSTOMER);
        users.setResetStatus("Unused");

        System.out.println("resetStatus trước khi lưu: " + users.getResetStatus());
        Users savedUsers = usersRepository.save(users);
        System.out.println("resetStatus sau khi lưu: " + savedUsers.getResetStatus());

        return "Đăng ký thành công!";
    }

    @Transactional
    public Users completeRegistration(UsersDTO usersDTO, String otp) {
        Optional<Users> userOptional = usersRepository.findByResetCode(otp);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ!");
        }

        Users user = userOptional.get();
        if (user.getResetExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn!");
        }

        if (user.isDeleted()) {
            user.setDeleted(false);
            user.setDeletedAt(null);
            user.setName(usersDTO.getName());
            user.setPassword(passwordEncoder.encode(usersDTO.getPassword()));
            user.setResetCode(null);
            user.setResetExpiry(null);
            return usersRepository.save(user);
        } else {
            throw new IllegalArgumentException("Tài khoản không cần khôi phục!");
        }
    }
    @Transactional
    public void changePassword(Authentication authentication, ChangePasswordDTO changePasswordDTO) {
        Integer userId = getUserIdFromAuthentication(authentication);
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng!");
        }

        // Kiểm tra mật khẩu mới
        if (changePasswordDTO.getNewPassword() == null || changePasswordDTO.getNewPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu mới không được để trống!");
        }
        if (changePasswordDTO.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự!");
        }

        // Kiểm tra xác nhận mật khẩu mới
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp!");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        usersRepository.save(user);
    }

    // Giữ nguyên các phương thức khác
    public Optional<Users> getActiveUserById(Integer id) {
        return usersRepository.findActiveById(id);
    }

    public List<Users> getAllActiveUsers() {
        return usersRepository.findAllActiveUsers();
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

    public void sendResetPasswordLink(ForgotPasswordDTO forgotPasswordDTO) {
        Optional<Users> usersOptional = usersRepository.findByEmail(forgotPasswordDTO.getEmail());
        if (usersOptional.isEmpty()) {
            throw new RuntimeException("Email không tồn tại trong hệ thống");
        }

        Users users = usersOptional.get();
        String resetCode = String.format("%06d", new Random().nextInt(999999));
        users.setResetCode(resetCode);
        users.setResetExpiry(LocalDateTime.ofInstant(Instant.now().plusSeconds(15 * 60), ZoneId.systemDefault()));
        usersRepository.save(users);

        emailService.sendEmail(users.getEmail(), "Đặt lại mật khẩu",
                "Mã OTP của bạn là: " + resetCode + ". Vui lòng nhập mã này trong ứng dụng để đặt lại mật khẩu.");
    }

    @Transactional
    public void deleteUser(Integer id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        usersRepository.save(user);
    }

    @Transactional
    public Users updateUserProfile(Authentication authentication, UserUpdateDTO updateDTO) {
        Integer userId = getUserIdFromAuthentication(authentication);
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        if (updateDTO.getName() != null && !updateDTO.getName().trim().isEmpty()) {
            user.setName(updateDTO.getName());
        }
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(updateDTO.getEmail()).matches()) {
                throw new IllegalArgumentException("Email không hợp lệ!");
            }
            if (!updateDTO.getEmail().equals(user.getEmail()) && usersRepository.existsByEmail(updateDTO.getEmail())) {
                throw new IllegalArgumentException("Email đã được sử dụng!");
            }
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getPhone() != null && !updateDTO.getPhone().trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(updateDTO.getPhone()).matches()) {
                throw new IllegalArgumentException("Số điện thoại phải có 10 số và bắt đầu bằng 0!");
            }
            if (!updateDTO.getPhone().equals(user.getPhone()) && usersRepository.existsByPhone(updateDTO.getPhone())) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng!");
            }
            user.setPhone(updateDTO.getPhone());
        }

        return usersRepository.save(user);
    }

    @Transactional
    public void deleteUserAccount(Authentication authentication) {
        Integer userId = getUserIdFromAuthentication(authentication);
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        usersRepository.save(user);
    }

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

    public Users getUserById(Integer id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }
}