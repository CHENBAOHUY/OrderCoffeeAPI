package com.example.springbootapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StatisticsRequestDTO {
    // Cho các filter theo ngày/thời gian
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @PastOrPresent(message = "Ngày bắt đầu không thể là tương lai")
    private LocalDateTime startDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @PastOrPresent(message = "Ngày kết thúc không thể là tương lai")
    private LocalDateTime endDateTime;

    // Cho thống kê theo tháng/năm
    @Min(value = 2000, message = "Năm không hợp lệ (tối thiểu: 2000)")
    private Integer year;

    @Min(value = 1, message = "Tháng phải từ 1-12")
    @jakarta.validation.constraints.Max(value = 12, message = "Tháng phải từ 1-12")
    private Integer month;

    // Cho giới hạn kết quả
    @Min(value = 1, message = "Limit phải lớn hơn 0")
    private Integer limit = 10;
}