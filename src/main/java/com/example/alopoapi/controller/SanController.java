package com.example.alopoapi.controller;

import com.example.alopoapi.service.SanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/san")
public class SanController {

    @Autowired
    SanService sanService;

    @GetMapping("/get-san/{maCumSan}")
    public ResponseEntity<?> getSanTheoCum(@PathVariable String maCumSan) {

        return ResponseEntity.ok(sanService.getSanTheoCum(maCumSan));

    }

    @GetMapping("/get-so-luong-san")
    public ResponseEntity<?> getSoLuongSan() {

        return ResponseEntity.ok(sanService.getSoLuongSan());

    }
}
