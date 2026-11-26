package com.example.alopoapi.controller;

import com.example.alopoapi.dto.request.CreatePhieuDatRequest;
import com.example.alopoapi.security.AccessGuard;
import com.example.alopoapi.service.PhieuDatSanService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

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

    /** Khách xin huỷ lượt đã thanh toán / đã xác nhận (trước giờ chơi ≥ 30 phút). Body: {lyDo}. */
    @PostMapping("/{maPhieuDat}/yeu-cau-huy")
    public ResponseEntity<?> requestCancel(@PathVariable String maPhieuDat, @RequestBody Map<String, String> body, Authentication authentication) {
        return ResponseEntity.ok(phieuDatSanService.requestCancel(maPhieuDat, AccessGuard.username(authentication), body == null ? null : body.get("lyDo")));
    }

    /** Khách rút lại yêu cầu huỷ khi chủ sân chưa xử lý. */
    @DeleteMapping("/{maPhieuDat}/yeu-cau-huy")
    public ResponseEntity<?> withdrawCancel(@PathVariable String maPhieuDat, Authentication authentication) {
        return ResponseEntity.ok(phieuDatSanService.withdrawCancel(maPhieuDat, AccessGuard.username(authentication)));
    }

    @DeleteMapping("/delete-phieu-dat/{maPhieuDat}")
    public ResponseEntity<?> deletePhieuDat(@PathVariable String maPhieuDat, Authentication authentication) {
        phieuDatSanService.deletePhieuDat(maPhieuDat, AccessGuard.username(authentication));
        return ResponseEntity.ok("Successful");
    }
}
