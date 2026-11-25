package com.example.alopoapi.controller;

import com.example.alopoapi.security.AccessGuard;
import com.example.alopoapi.service.TranDauService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Trận / kèo và ghép trận vãng lai. */
@RestController
public class TranDauController {

    private final TranDauService tranDauService;

    public TranDauController(TranDauService tranDauService) {
        this.tranDauService = tranDauService;
    }

    /** Công khai: tìm trận vãng lai. ngay = yyyy-MM-dd; conCho = chỉ trận còn chỗ. */
    @GetMapping("/tran/tim-kiem")
    public ResponseEntity<?> search(@RequestParam(required = false) String maCumSan,
                                    @RequestParam(required = false) String ngay,
                                    @RequestParam(required = false) String trinhDo,
                                    @RequestParam(required = false) String kieuChoi,
                                    @RequestParam(defaultValue = "false") boolean conCho,
                                    Authentication authentication) {
        return ResponseEntity.ok(tranDauService.search(maCumSan, ngay, trinhDo, kieuChoi, conCho,
                authentication == null ? null : authentication.getName()));
    }

    @GetMapping("/tran/cua-toi")
    public ResponseEntity<?> mine(Authentication authentication) {
        return ResponseEntity.ok(tranDauService.mine(AccessGuard.username(authentication)));
    }

    /** Lượt đặt sân sắp tới chưa có trận, để tạo trận. */
    @GetMapping("/tran/phieu-dat-kha-dung")
    public ResponseEntity<?> eligibleBookings(Authentication authentication) {
        return ResponseEntity.ok(tranDauService.eligibleBookings(AccessGuard.username(authentication)));
    }

    /**
     * {maPhieuDat, maNhom?, tieuDe?, ghiChu?, kieuChoi?, soNguoiToiDa?, trinhDoTu?, trinhDoDen?,
     *  chiPhiMoiNguoi?, congKhai?, nganHang?, soTaiKhoan?, chuTaiKhoan?}
     */
    @PostMapping("/tran")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.ok(tranDauService.create(AccessGuard.username(authentication), body));
    }

    @GetMapping("/tran/{id}")
    public ResponseEntity<?> detail(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(tranDauService.detail(id, AccessGuard.username(authentication)));
    }

    @PutMapping("/tran/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.ok(tranDauService.update(id, AccessGuard.username(authentication), body));
    }

    @PostMapping("/tran/{id}/huy")
    public ResponseEntity<?> cancel(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(tranDauService.cancel(id, AccessGuard.username(authentication)));
    }

    /** {loiNhan?, trinhDo?} */
    @PostMapping("/tran/{id}/tham-gia")
    public ResponseEntity<?> join(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.ok(tranDauService.join(id, AccessGuard.username(authentication), body == null ? Map.of() : body));
    }

    @PostMapping("/tran/{id}/roi")
    public ResponseEntity<?> leave(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(tranDauService.leave(id, AccessGuard.username(authentication)));
    }

    /** {dongY: true|false} */
    @PostMapping("/tran/{id}/duyet/{username}")
    public ResponseEntity<?> review(@PathVariable String id, @PathVariable String username, @RequestBody Map<String, Boolean> body, Authentication authentication) {
        return ResponseEntity.ok(tranDauService.review(id, AccessGuard.username(authentication), username, Boolean.TRUE.equals(body.get("dongY"))));
    }

    @DeleteMapping("/tran/{id}/nguoi-choi/{username}")
    public ResponseEntity<?> kick(@PathVariable String id, @PathVariable String username, Authentication authentication) {
        return ResponseEntity.ok(tranDauService.kick(id, AccessGuard.username(authentication), username));
    }

    /** Người chơi báo đã chuyển tiền: {billUrl?} */
    @PostMapping("/tran/{id}/thanh-toan")
    public ResponseEntity<?> pay(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.ok(tranDauService.pay(id, AccessGuard.username(authentication), body == null ? Map.of() : body));
    }

    /** Chủ trận xác nhận: {daTra: true|false} */
    @PostMapping("/tran/{id}/thanh-toan/{username}")
    public ResponseEntity<?> confirmPayment(@PathVariable String id, @PathVariable String username, @RequestBody Map<String, Boolean> body, Authentication authentication) {
        return ResponseEntity.ok(tranDauService.confirmPayment(id, AccessGuard.username(authentication), username, Boolean.TRUE.equals(body.get("daTra"))));
    }

    /** Chủ sân / admin: trận ở các cụm sân mình quản lý. */
    @GetMapping("/management/matches")
    public ResponseEntity<?> managed(@RequestParam(required = false) String venue, Authentication authentication) {
        return ResponseEntity.ok(tranDauService.managed(venue, authentication));
    }
}
