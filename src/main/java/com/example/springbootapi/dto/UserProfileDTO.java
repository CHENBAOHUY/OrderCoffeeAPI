package com.example.springbootapi.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private String name;
    private String phone;
    private String email;
    private int points;

    // Constructor
    public UserProfileDTO(String name, String phone, String email, int points) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.points = points;
    }
}