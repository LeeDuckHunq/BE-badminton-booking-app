package com.example.alopoapi.controller;

import com.example.alopoapi.security.AccessGuard;
import com.example.alopoapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Công khai: chỉ trả về một con số thống kê. */
    @GetMapping("/get-so-luong-user")
    public ResponseEntity<?> getSoLuongUser() {
        return ResponseEntity.ok(userService.getSoLuongUser());
    }

    @GetMapping("/get-user-info/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username, Authentication authentication) {
        AccessGuard.requireSelfOrAdmin(authentication, username);
        return ResponseEntity.ok(userService.requireUser(username));
    }

    /** Danh bạ cho tính năng chat (chỉ username + họ tên, yêu cầu đăng nhập). */
    @GetMapping("/get-all-user")
    public ResponseEntity<?> getAllUser() {
        return ResponseEntity.ok(userService.getAllUser());
    }
}
