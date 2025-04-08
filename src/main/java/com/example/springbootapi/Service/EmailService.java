package com.example.springbootapi.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Phương thức gửi email đặt lại mật khẩu với OTP (HTML)
    public void sendResetPasswordEmail(String toEmail, String otp) {
        sendEmail(toEmail, "Đặt lại mật khẩu của bạn",
                "<p>Mã OTP của bạn là: <strong>" + otp + "</strong>.</p>" +
                        "<p>Vui lòng nhập mã này trong ứng dụng để đặt lại mật khẩu. Mã có hiệu lực trong 15 phút.</p>");
    }

    // Phương thức gửi email chung (Plain Text hoặc HTML)
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true để hỗ trợ HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }
}