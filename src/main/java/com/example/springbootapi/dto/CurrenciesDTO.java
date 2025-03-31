package com.example.springbootapi.dto;

import lombok.Data;

@Data
public class CurrenciesDTO {
    private Integer id;
    private String currencyCode;
    private String currencyName;

}
