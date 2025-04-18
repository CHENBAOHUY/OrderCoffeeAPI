package com.example.springbootapi.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {

    @NotNull(message = "Mã người dùng không được để trống")
    private Integer userId;

    @NotNull(message = "Mã đơn hàng không được để trống")
    private Integer orderId;

    @NotNull(message = "Mã sản phẩm không được để trống")
    private Integer productId;

    @NotNull(message = "Điểm đánh giá không được để trống")
    @Min(value = 1, message = "Điểm đánh giá phải từ 1 đến 5")
    @Max(value = 5, message = "Điểm đánh giá phải từ 1 đến 5")
    private Integer rating;

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    @Size(max = 1000, message = "Nội dung đánh giá không được vượt quá 1000 ký tự")
    private String comment;

    @Size(max = 200, message = "Tiêu đề đánh giá không được vượt quá 200 ký tự")
    private String title;
}