package com.example.springbootapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VnPayCallbackDTO {
    private String vnp_TxnRef;
    private String vnp_Amount;
    private String vnp_ResponseCode;
    private String vnp_TransactionNo;
    private String vnp_BankCode;
    private String vnp_CardType;
    private String vnp_PayDate;
    private String vnp_OrderInfo;
}