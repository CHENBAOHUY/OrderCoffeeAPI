package com.example.springbootapi.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private Integer id; // Thêm field id
    private String name;
    private String phone;
    private String email;
    private int points;

    // Constructor
    public UserProfileDTO(Integer id, String name, String phone, String email, int points) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.points = points;
    }

    // Constructor mặc định (cần thiết cho một số trường hợp ánh xạ)
    public UserProfileDTO() {
    }
}