package com.example.alopoapi.controller;

import com.example.alopoapi.dto.request.CreatePhieuDatRequest;
import com.example.alopoapi.security.AccessGuard;
import com.example.alopoapi.service.PhieuDatSanService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/phieu-dat-san")
public class PhieuDatSanController {

    private final PhieuDatSanService phieuDatSanService;

    public PhieuDatSanController(PhieuDatSanService phieuDatSanService) {
        this.phieuDatSanService = phieuDatSanService;
    }

    /** Công khai: các khung giờ đã đặt trong ngày (không kèm thông tin người đặt). */
    @GetMapping("/theo-ngay")
    public ResponseEntity<?> getTheoNgay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay
    ) {
        return ResponseEntity.ok(phieuDatSanService.getPhieuDatTheoNgay(ngay));
    }

    /** Người đặt luôn là tài khoản đang đăng nhập; trường maNguoiDung trong body bị bỏ qua. */
    @PostMapping("/create")
    public ResponseEntity<?> createPhieuDat(@RequestBody CreatePhieuDatRequest request, Authentication authentication) {
        return ResponseEntity.ok(phieuDatSanService.createPhieuDat(request, AccessGuard.username(authentication)));
    }

    @GetMapping("/get-theo-user/{username}")
    public ResponseEntity<?> getPhieuDatForUser(@PathVariable String username, Authentication authentication) {
        AccessGuard.requireSelfOrAdmin(authentication, username);
        return ResponseEntity.ok(phieuDatSanService.getPhieuDatForUser(username));
    }

    @DeleteMapping("/delete-phieu-dat/{maPhieuDat}")
    public ResponseEntity<?> deletePhieuDat(@PathVariable String maPhieuDat, Authentication authentication) {
        phieuDatSanService.deletePhieuDat(maPhieuDat, AccessGuard.username(authentication));
        return ResponseEntity.ok("Successful");
    }
}
