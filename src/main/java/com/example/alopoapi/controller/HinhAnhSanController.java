package com.example.alopoapi.controller;

import com.example.alopoapi.service.HinhAnhSanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HinhAnhSanController {

    @Autowired
    private HinhAnhSanService hinhAnhSanService;

    @GetMapping("/anh-san/{maCumSan}")
    public ResponseEntity<?> getHinhAnhSan(@PathVariable String maCumSan) {

        var result = hinhAnhSanService.getHinhAnh(maCumSan);

        return ResponseEntity.ok(result);

    }
}
