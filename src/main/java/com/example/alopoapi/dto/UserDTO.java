package com.example.alopoapi.dto;

/** Thông tin công khai của một tài khoản (danh bạ chat). */
public class UserDTO {

    public String username;
    public String fullName;
    public String role;

    public UserDTO(String username, String fullName, String role) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }
}
