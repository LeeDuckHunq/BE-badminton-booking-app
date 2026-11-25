package com.example.alopoapi.controller;

import com.example.alopoapi.service.PhienBanAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Phiên bản app: app hỏi bản mới khi mở, web trang tải app, admin đăng bản phát hành. */
@RestController
public class PhienBanAppController {

    private final PhienBanAppService service;

    public PhienBanAppController(PhienBanAppService service) {
        this.service = service;
    }

    /** Công khai. build = build của app đang chạy (để biết có bản mới / bắt buộc cập nhật). */
    @GetMapping("/app/phien-ban")
    public ResponseEntity<?> latest(@RequestParam(defaultValue = "android") String nenTang, @RequestParam(required = false) Integer build) {
        return ResponseEntity.ok(service.latest(nenTang, build));
    }

    /** Công khai: lịch sử phát hành (trang tải app). */
    @GetMapping("/app/phien-ban/lich-su")
    public ResponseEntity<?> history(@RequestParam(defaultValue = "android") String nenTang) {
        return ResponseEntity.ok(service.history(nenTang));
    }

    @GetMapping("/management/admin/app-versions")
    public ResponseEntity<?> list(@RequestParam(defaultValue = "android") String nenTang) {
        return ResponseEntity.ok(service.history(nenTang));
    }

    /** {phienBan, soBuild, buildToiThieu, thayDoi: [..], taiVe: {abi: url}, kichThuoc: {abi: bytes}} */
    @PostMapping("/management/admin/app-versions")
    public ResponseEntity<?> publish(@RequestBody Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.ok(service.publish(body, authentication.getName()));
    }

    @DeleteMapping("/management/admin/app-versions/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
