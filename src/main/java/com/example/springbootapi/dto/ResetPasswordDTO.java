package com.example.springbootapi.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class ResetPasswordDTO {
    private String email;
    private String resetCode;

    @NotBlank(message = "Mật khẩu mới không được để trống!")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự!")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu mới không được để trống!")
    private String confirmPassword;

    @AssertTrue(message = "Mật khẩu xác nhận không khớp với mật khẩu mới!")
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }


    // Getters và Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getResetCode() { return resetCode; }
    public void setResetCode(String resetCode) { this.resetCode = resetCode; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}