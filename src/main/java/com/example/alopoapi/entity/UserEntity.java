package com.example.alopoapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "users")
public class UserEntity {

    //region declare
    @Id
    private String username;

    @Field("password")
    @JsonIgnore
    private String password;

    @Field("role")
    private String role;

    @Field("full_name")
    private String fullName;

    @Field("email")
    private String email;

    @Field("phone_number")
    private String phoneNumber;

    @Field("active")
    private Boolean active;
    //endregion

    //region constructor

    public UserEntity() {
    }

    public UserEntity(String username, String password, String role,
                      String fullName, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.active = true;
    }
    //endregion

    //region getter-setter

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isActive() {
        return active == null || active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    //endregion
}
