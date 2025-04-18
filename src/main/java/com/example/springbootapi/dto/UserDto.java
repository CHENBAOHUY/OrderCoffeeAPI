package com.example.springbootapi.dto;


import lombok.Data;

@Data
public class UserDto {
    private Integer id;
    private String name;

    public UserDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }


}