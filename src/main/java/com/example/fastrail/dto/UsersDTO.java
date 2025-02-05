package com.example.fastrail.dto;

import lombok.Getter;
import lombok.Setter;

//@Getter
//@Setter
public class UsersDTO {

    private String email;
    private String password;
    private String name;
    private String phone;
    private String twId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTwId() {
        return twId;
    }

    public void setTwId(String twId) {
        this.twId = twId;
    }
}
