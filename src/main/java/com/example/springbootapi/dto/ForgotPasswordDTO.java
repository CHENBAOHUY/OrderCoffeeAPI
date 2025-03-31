package com.example.springbootapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordDTO {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    // Getters, setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}