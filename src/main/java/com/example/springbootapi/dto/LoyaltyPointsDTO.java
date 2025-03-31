package com.example.springbootapi.dto;

import lombok.Data;

@Data
public class LoyaltyPointsDTO {
    private Integer id;
    private Integer userId;
    private Integer points;
    private String description;
    private Long dateAdded;

}
