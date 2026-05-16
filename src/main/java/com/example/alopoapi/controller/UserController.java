package com.example.alopoapi.controller;

import com.example.alopoapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/get-so-luong-user")
    public ResponseEntity<?> getSoLuongUser() {

        return ResponseEntity.ok(userService.getSoLuongUser());

    }
}
