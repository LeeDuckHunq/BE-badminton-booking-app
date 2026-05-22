package com.example.alopoapi.controller;

import com.example.alopoapi.dto.request.NhanKhuyenMaiRequest;
import com.example.alopoapi.service.KhuyenMaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/khuyen-mai")
public class KhuyenMaiController {

    @Autowired
    private KhuyenMaiService khuyenMaiService;

    @GetMapping("/get-khuyen-mai/{username}")
    public ResponseEntity<?> getKhuyenMaiTheoUser(@PathVariable String username) {

        return ResponseEntity.ok(khuyenMaiService.getKhuyenMaiCuaUser(username));
    }

    @GetMapping("/chua-co/{username}")
    public ResponseEntity<?> getKhuyenMaiChuaCo(
            @PathVariable String username) {

        return ResponseEntity.ok(khuyenMaiService.getKhuyenMaiChuaCo(username)
        );
    }

    @PostMapping("/nhan")
    public ResponseEntity<?> nhanKhuyenMai(
            @RequestBody NhanKhuyenMaiRequest request) {

        return ResponseEntity.ok(
                khuyenMaiService.nhanKhuyenMai(
                        request.getUsername(),
                        request.getMaKhuyenMai()
                )
        );
    }
}
