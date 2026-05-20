package com.example.alopoapi.controller;

import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.security.JwtGenerateToken;
import com.example.alopoapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> user) {

        if (userService.login(
                user.get("username"),
                user.get("password"))) {

            Map<String, String> map = new HashMap<>();

            map.put("token", JwtGenerateToken.generateToken(user.get("username"), "user"));

            return ResponseEntity.ok(map);
        }
        return ResponseEntity.badRequest().body("Fail!");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> newUser) {

        if (userService.register(newUser)) {
            return ResponseEntity.ok("");
        }

        return ResponseEntity.badRequest().body("Fail!");
    }
}
