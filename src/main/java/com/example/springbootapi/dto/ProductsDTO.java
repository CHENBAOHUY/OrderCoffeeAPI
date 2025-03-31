package com.example.springbootapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class ProductsDTO {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private Integer id;
    private String name;
    private String description;
    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá sản phẩm phải lớn hơn 0")
    private BigDecimal price;
    private String image;
    @NotNull(message = "Danh mục sản phẩm không được để trống")
    private Integer categoryId;
    private Boolean isDeleted;

}
