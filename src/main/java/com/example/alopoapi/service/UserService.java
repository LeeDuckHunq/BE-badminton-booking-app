package com.example.alopoapi.service;

import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public boolean register(Map<String, String> newUser) {

        for (var user : userRepo.findAll()) {
            if (user.getUsername().equals(newUser.get("username"))) return false;
        }

        userRepo.save(new UserEntity(
                newUser.get("username"),
                newUser.get("password"),
                newUser.get("role"),
                newUser.get("fullName"),
                newUser.get("email"),
                newUser.get("phoneNumber")
        ));

        return true;
    }

    public boolean login(String username, String password) {

        for (var user : userRepo.findAll()) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                if (user.getPassword().equals(password)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean register() {
        return true;
    }

    public int getSoLuongUser() {

        return (int)userRepo.count();

    }
}
