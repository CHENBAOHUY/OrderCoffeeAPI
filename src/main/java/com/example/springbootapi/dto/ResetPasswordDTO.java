package com.example.springbootapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordDTO {
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Mã reset không được để trống")
    private String resetCode;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 7, message = "Mật khẩu mới phải có ít nhất 7 ký tự")
    private String newPassword;

    // Getters, setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getResetCode() { return resetCode; }
    public void setResetCode(String resetCode) { this.resetCode = resetCode; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}