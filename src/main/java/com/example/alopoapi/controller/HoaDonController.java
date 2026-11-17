package com.example.alopoapi.controller;

import com.example.alopoapi.dto.request.CreateHoaDonRequest;
import com.example.alopoapi.security.AccessGuard;
import com.example.alopoapi.service.HoaDonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hoa-don")
public class HoaDonController {

    private final HoaDonService hoaDonService;

    public HoaDonController(HoaDonService hoaDonService) {
        this.hoaDonService = hoaDonService;
    }

    @PostMapping("/tao-hoa-don")
    public ResponseEntity<?> addHoaDon(@RequestBody CreateHoaDonRequest request, Authentication authentication) {
        return ResponseEntity.ok(hoaDonService.createHoaDon(request, AccessGuard.username(authentication)));
    }

    @GetMapping("/get-hoa-don/{maPhieuDat}")
    public ResponseEntity<?> getHoaDon(@PathVariable String maPhieuDat, Authentication authentication) {
        return ResponseEntity.ok(hoaDonService.getHoaDon(maPhieuDat, authentication));
    }
}
