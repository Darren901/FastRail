package com.example.fastrail.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UsersDTO {

    private String email;
    private String password;
    private String name;
    private String phone;
    private String twId;
    private String otp;


}
