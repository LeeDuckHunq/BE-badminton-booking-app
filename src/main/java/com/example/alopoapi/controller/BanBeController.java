package com.example.alopoapi.controller;

import com.example.alopoapi.service.BanBeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Bạn bè: danh sách, tìm người, lời mời kết bạn; hồ sơ công khai để mở chat. */
@RestController
@RequestMapping("/ban-be")
public class BanBeController {

    private final BanBeService service;

    public BanBeController(BanBeService service) {
        this.service = service;
    }

    /** {banBe, loiMoiDen, loiMoiDi} */
    @GetMapping
    public ResponseEntity<?> overview(Authentication authentication) {
        return ResponseEntity.ok(service.overview(authentication.getName()));
    }

    /** Tìm theo số điện thoại đầy đủ, hoặc tên đăng nhập / họ tên (≥ 3 ký tự). Kèm quanHe: KHONG, DA_GUI, DUOC_MOI, BAN_BE. */
    @GetMapping("/tim")
    public ResponseEntity<?> search(@RequestParam(defaultValue = "") String q, Authentication authentication) {
        return ResponseEntity.ok(service.search(authentication.getName(), q));
    }

    /** {username, fullName, role, quanHe, coTheNhan} */
    @GetMapping("/ho-so/{username}")
    public ResponseEntity<?> profile(@PathVariable String username, Authentication authentication) {
        return ResponseEntity.ok(service.publicProfile(authentication.getName(), username));
    }

    /** Body: {username} */
    @PostMapping("/loi-moi")
    public ResponseEntity<?> request(@RequestBody Map<String, String> body, Authentication authentication) {
        return ResponseEntity.ok(service.request(authentication.getName(), body == null ? null : body.get("username")));
    }

    @PostMapping("/{username}/dong-y")
    public ResponseEntity<?> accept(@PathVariable String username, Authentication authentication) {
        return ResponseEntity.ok(service.accept(authentication.getName(), username));
    }

    /** Từ chối / huỷ lời mời / huỷ kết bạn. */
    @DeleteMapping("/{username}")
    public ResponseEntity<?> remove(@PathVariable String username, Authentication authentication) {
        service.remove(authentication.getName(), username);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
