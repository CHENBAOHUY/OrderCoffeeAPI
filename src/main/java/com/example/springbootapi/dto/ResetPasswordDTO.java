package com.example.springbootapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotBlank(message = "Mã reset không được để trống")
    private String resetCode;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 7, message = "Mật khẩu mới phải có ít nhất 7 ký tự")
    private String newPassword;
}