package com.example.alopoapi.controller;

import com.example.alopoapi.security.AccessGuard;
import com.example.alopoapi.service.NhomService;
import com.example.alopoapi.service.TranDauService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Hội nhóm. */
@RestController
public class NhomController {

    private final NhomService nhomService;
    private final TranDauService tranDauService;

    public NhomController(NhomService nhomService, TranDauService tranDauService) {
        this.nhomService = nhomService;
        this.tranDauService = tranDauService;
    }

    /** Công khai: nhóm để khám phá. Có đăng nhập thì kèm trạng thái của tôi. */
    @GetMapping("/nhom/kham-pha")
    public ResponseEntity<?> discover(@RequestParam(required = false) String q, @RequestParam(required = false) String trinhDo, Authentication authentication) {
        return ResponseEntity.ok(nhomService.discover(q, trinhDo, authentication == null ? null : authentication.getName()));
    }

    @GetMapping("/nhom/cua-toi")
    public ResponseEntity<?> mine(Authentication authentication) {
        return ResponseEntity.ok(nhomService.myGroups(AccessGuard.username(authentication)));
    }

    /** {tenNhom, moTa, khuVuc, trinhDo, congKhai, mau} */
    @PostMapping("/nhom")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.ok(nhomService.create(AccessGuard.username(authentication), body));
    }

    @GetMapping("/nhom/{id}")
    public ResponseEntity<?> detail(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(nhomService.detail(id, AccessGuard.username(authentication)));
    }

    @PutMapping("/nhom/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.ok(nhomService.update(id, AccessGuard.username(authentication), body));
    }

    @DeleteMapping("/nhom/{id}")
    public ResponseEntity<?> disband(@PathVariable String id, Authentication authentication) {
        nhomService.disband(id, AccessGuard.username(authentication));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** {maMoi?, loiNhan?} */
    @PostMapping("/nhom/{id}/tham-gia")
    public ResponseEntity<?> join(@PathVariable String id, @RequestBody(required = false) Map<String, String> body, Authentication authentication) {
        Map<String, String> b = body == null ? Map.of() : body;
        return ResponseEntity.ok(nhomService.join(id, AccessGuard.username(authentication), b.get("maMoi"), b.get("loiNhan")));
    }

    /** {maMoi} */
    @PostMapping("/nhom/tham-gia-bang-ma")
    public ResponseEntity<?> joinByCode(@RequestBody Map<String, String> body, Authentication authentication) {
        return ResponseEntity.ok(nhomService.joinByCode(AccessGuard.username(authentication), body.get("maMoi")));
    }

    @DeleteMapping("/nhom/{id}/yeu-cau")
    public ResponseEntity<?> cancelRequest(@PathVariable String id, Authentication authentication) {
        nhomService.cancelRequest(id, AccessGuard.username(authentication));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/nhom/{id}/roi")
    public ResponseEntity<?> leave(@PathVariable String id, Authentication authentication) {
        nhomService.leave(id, AccessGuard.username(authentication));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** {dongY: true|false} */
    @PostMapping("/nhom/{id}/duyet/{username}")
    public ResponseEntity<?> review(@PathVariable String id, @PathVariable String username, @RequestBody Map<String, Boolean> body, Authentication authentication) {
        return ResponseEntity.ok(nhomService.review(id, AccessGuard.username(authentication), username, Boolean.TRUE.equals(body.get("dongY"))));
    }

    /** {vaiTro: PHO_NHOM | THANH_VIEN | TRUONG_NHOM (chuyển quyền)} */
    @PostMapping("/nhom/{id}/vai-tro/{username}")
    public ResponseEntity<?> role(@PathVariable String id, @PathVariable String username, @RequestBody Map<String, String> body, Authentication authentication) {
        return ResponseEntity.ok(nhomService.setRole(id, AccessGuard.username(authentication), username, body.get("vaiTro")));
    }

    @DeleteMapping("/nhom/{id}/thanh-vien/{username}")
    public ResponseEntity<?> kick(@PathVariable String id, @PathVariable String username, Authentication authentication) {
        return ResponseEntity.ok(nhomService.kick(id, AccessGuard.username(authentication), username));
    }

    @PostMapping("/nhom/{id}/ma-moi")
    public ResponseEntity<?> regenerateCode(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(nhomService.regenerateCode(id, AccessGuard.username(authentication)));
    }

    @GetMapping("/nhom/{id}/tin-nhan")
    public ResponseEntity<?> messages(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(nhomService.messages(id, AccessGuard.username(authentication)));
    }

    /** {noiDung} */
    @PostMapping("/nhom/{id}/tin-nhan")
    public ResponseEntity<?> send(@PathVariable String id, @RequestBody Map<String, String> body, Authentication authentication) {
        return ResponseEntity.ok(nhomService.send(id, AccessGuard.username(authentication), body.get("noiDung")));
    }

    @GetMapping("/nhom/{id}/tran")
    public ResponseEntity<?> matches(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(tranDauService.groupMatches(id, AccessGuard.username(authentication)));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @GetMapping("/management/admin/groups")
    public ResponseEntity<?> adminList() {
        return ResponseEntity.ok(nhomService.adminList());
    }

    /** {khoa: true|false} */
    @PatchMapping("/management/admin/groups/{id}/lock")
    public ResponseEntity<?> lock(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(nhomService.setLocked(id, Boolean.TRUE.equals(body.get("khoa"))));
    }
}
