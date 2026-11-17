package com.example.alopoapi.controller;

import com.example.alopoapi.dto.request.NhanKhuyenMaiRequest;
import com.example.alopoapi.security.AccessGuard;
import com.example.alopoapi.service.KhuyenMaiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/khuyen-mai")
public class KhuyenMaiController {

    private final KhuyenMaiService khuyenMaiService;

    public KhuyenMaiController(KhuyenMaiService khuyenMaiService) {
        this.khuyenMaiService = khuyenMaiService;
    }

    @GetMapping("/get-khuyen-mai/{username}")
    public ResponseEntity<?> getKhuyenMaiTheoUser(@PathVariable String username, Authentication authentication) {
        AccessGuard.requireSelfOrAdmin(authentication, username);
        return ResponseEntity.ok(khuyenMaiService.getKhuyenMaiCuaUser(username));
    }

    @GetMapping("/chua-co/{username}")
    public ResponseEntity<?> getKhuyenMaiChuaCo(@PathVariable String username, Authentication authentication) {
        AccessGuard.requireSelfOrAdmin(authentication, username);
        return ResponseEntity.ok(khuyenMaiService.getKhuyenMaiChuaCo(username));
    }

    /** Voucher luôn được nhận cho tài khoản đang đăng nhập; trường username trong body bị bỏ qua. */
    @PostMapping("/nhan")
    public ResponseEntity<?> nhanKhuyenMai(@RequestBody NhanKhuyenMaiRequest request, Authentication authentication) {
        return ResponseEntity.ok(khuyenMaiService.nhanKhuyenMai(
                AccessGuard.username(authentication),
                request == null ? null : request.getMaKhuyenMai()
        ));
    }
}
