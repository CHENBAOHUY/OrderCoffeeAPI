package com.example.springbootapi.dto;


import lombok.Data;

@Data
public class UserSummaryDTO {
    private Integer id;
    private String name;
    private int points;

    public UserSummaryDTO(Integer id, String name, int points) {
        this.id = id;
        this.name = name;
        this.points = points;
    }
}