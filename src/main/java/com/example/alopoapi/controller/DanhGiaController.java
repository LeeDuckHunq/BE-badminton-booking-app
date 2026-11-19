package com.example.alopoapi.controller;

import com.example.alopoapi.security.AccessGuard;
import com.example.alopoapi.service.DanhGiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class DanhGiaController {

    private final DanhGiaService danhGiaService;

    public DanhGiaController(DanhGiaService danhGiaService) {
        this.danhGiaService = danhGiaService;
    }

    // ── Khách ────────────────────────────────────────────────────────────────

    /** Gửi / sửa đánh giá cho một lần chơi: {maPhieuDat, soSao (1-5), noiDung}. */
    @PostMapping("/danh-gia")
    public ResponseEntity<?> submit(@RequestBody Map<String, Object> body, Authentication authentication) {
        Object stars = body.get("soSao");
        int soSao = stars instanceof Number n ? n.intValue() : 0;
        return ResponseEntity.ok(danhGiaService.submit(
                AccessGuard.username(authentication),
                (String) body.get("maPhieuDat"),
                soSao,
                (String) body.get("noiDung")));
    }

    @GetMapping("/danh-gia/cua-toi")
    public ResponseEntity<?> mine(Authentication authentication) {
        return ResponseEntity.ok(danhGiaService.myReviews(AccessGuard.username(authentication)));
    }

    /** Công khai: đánh giá của một cụm sân. */
    @GetMapping("/danh-gia/cum-san/{maCumSan}")
    public ResponseEntity<?> venue(@PathVariable String maCumSan) {
        return ResponseEntity.ok(danhGiaService.venueReviews(maCumSan));
    }

    // ── Quản lý ──────────────────────────────────────────────────────────────

    @GetMapping("/management/reviews")
    public ResponseEntity<?> managed(@RequestParam(required = false) String venue, Authentication authentication) {
        return ResponseEntity.ok(danhGiaService.managedReviews(venue, authentication));
    }

    @PatchMapping("/management/reviews/{id}/reply")
    public ResponseEntity<?> reply(@PathVariable String id, @RequestBody Map<String, String> body, Authentication authentication) {
        return ResponseEntity.ok(danhGiaService.reply(id, body.get("phanHoi"), authentication));
    }

    /** Chỉ admin (đường dẫn /management/admin/**). */
    @PatchMapping("/management/admin/reviews/{id}/visibility")
    public ResponseEntity<?> visibility(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(danhGiaService.setHidden(id, Boolean.TRUE.equals(body.get("an"))));
    }
}
