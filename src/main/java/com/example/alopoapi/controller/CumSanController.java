package com.example.alopoapi.controller;

import com.example.alopoapi.service.CumSanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CumSanController {

    @Autowired
    private CumSanService cumSanService;

    @GetMapping("/cumsan/{amount}")
    public ResponseEntity<?> getCumSan(@PathVariable int amount) {
        return ResponseEntity.ok(cumSanService.getCumSan(amount));
    }
}
