package com.example.alopoapi.controller;

import com.example.alopoapi.service.KhuyenMaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/khuyen-mai")
public class KhuyenMaiController {

    @Autowired
    private KhuyenMaiService khuyenMaiService;

    @GetMapping("/get-khuyen-mai/{username}")
    public ResponseEntity<?> getKhuyenMaiTheoUser(@PathVariable String username) {

        return ResponseEntity.ok(khuyenMaiService.getKhuyenMaiCuaUser(username));
    }
}
