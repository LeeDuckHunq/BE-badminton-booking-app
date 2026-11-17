package com.example.alopoapi.controller;

import com.example.alopoapi.service.SanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API đọc công khai cho app khách hàng.
 * Thêm / sửa / ngừng sân được thực hiện qua /management/courts (yêu cầu quyền ADMIN/OWNER).
 */
@RestController
@RequestMapping("/san")
public class SanController {

    private final SanService sanService;

    public SanController(SanService sanService) {
        this.sanService = sanService;
    }

    @GetMapping("/get-san/{maCumSan}")
    public ResponseEntity<?> getSanTheoCum(@PathVariable String maCumSan) {
        return ResponseEntity.ok(sanService.getSanTheoCum(maCumSan));
    }

    @GetMapping("/get-so-luong-san")
    public ResponseEntity<?> getSoLuongSan() {
        return ResponseEntity.ok(sanService.getSoLuongSan());
    }

    @GetMapping("/san-info/{maSan}")
    public ResponseEntity<?> getSanInfo(@PathVariable String maSan) {
        return ResponseEntity.ok(sanService.getSanInfo(maSan));
    }
}
