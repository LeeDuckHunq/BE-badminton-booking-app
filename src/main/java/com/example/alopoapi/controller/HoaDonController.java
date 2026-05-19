package com.example.alopoapi.controller;

import com.example.alopoapi.entity.HoaDonEntity;
import com.example.alopoapi.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hoa-don")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @PostMapping("/tao-hoa-don")
    public ResponseEntity<?> addHoaDon(@RequestBody HoaDonEntity hoadon) {

        hoaDonService.addHoaDon(hoadon);

        return ResponseEntity.ok("Tao thanh cong!");
    }
}
