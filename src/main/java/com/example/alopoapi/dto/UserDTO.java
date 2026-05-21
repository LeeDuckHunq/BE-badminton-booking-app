package com.example.alopoapi.dto;

public class UserDTO {

    public String username;
    public String fullName;

    //region constructor

    public UserDTO(String username, String fullName) {
        this.username = username;
        this.fullName = fullName;
    }

    //endregion
}
