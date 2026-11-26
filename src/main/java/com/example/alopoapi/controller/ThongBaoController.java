package com.example.alopoapi.controller;

import com.example.alopoapi.service.ThongBaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Thông báo của người đang đăng nhập (khách, chủ sân, admin). */
@RestController
@RequestMapping("/thong-bao")
public class ThongBaoController {

    private final ThongBaoService service;

    public ThongBaoController(ThongBaoService service) {
        this.service = service;
    }

    /** {chuaDoc, items: [60 thông báo mới nhất]} */
    @GetMapping
    public ResponseEntity<?> list(Authentication authentication) {
        return ResponseEntity.ok(service.list(authentication.getName()));
    }

    @GetMapping("/chua-doc")
    public ResponseEntity<?> unread(Authentication authentication) {
        return ResponseEntity.ok(service.unread(authentication.getName()));
    }

    /** App Android lấy thông báo chưa đẩy để hiện thành thông báo hệ thống (gọi xong coi như đã gửi). */
    @GetMapping("/chua-gui")
    public ResponseEntity<?> pending(Authentication authentication) {
        return ResponseEntity.ok(service.takePending(authentication.getName()));
    }

    @PostMapping("/{id}/da-doc")
    public ResponseEntity<?> read(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(service.markRead(id, authentication.getName()));
    }

    @PostMapping("/da-doc-het")
    public ResponseEntity<?> readAll(Authentication authentication) {
        return ResponseEntity.ok(service.markAllRead(authentication.getName()));
    }
}
