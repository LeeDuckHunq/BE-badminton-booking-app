package com.example.alopoapi.controller;

import com.example.alopoapi.security.JwtService;
import com.example.alopoapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class AuthenticationController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthenticationController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        return userService.authenticate(request.get("username"), request.get("password"))
                .<ResponseEntity<?>>map(user -> {
                    String role = userService.normalizeRole(user.getRole());
                    Map<String, String> response = new LinkedHashMap<>();
                    response.put("token", jwtService.generateToken(user.getUsername(), role));
                    response.put("username", user.getUsername());
                    response.put("role", role);
                    response.put("fullName", user.getFullName());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("message", "Sai tên đăng nhập hoặc mật khẩu")));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> newUser) {
        if (userService.register(newUser)) {
            return ResponseEntity.ok(Map.of("message", "Đăng ký thành công"));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "Thông tin đăng ký không hợp lệ"));
    }
}
