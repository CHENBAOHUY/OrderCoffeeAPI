package com.example.springbootapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoriesDTO {
    private Integer id;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 3, max = 100, message = "Tên danh mục phải từ 3 đến 100 ký tự")
    private String name;

    private String image;

    private String description;
}