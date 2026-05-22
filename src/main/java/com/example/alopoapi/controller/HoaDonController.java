package com.example.alopoapi.controller;

import com.example.alopoapi.entity.HoaDonEntity;
import com.example.alopoapi.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hoa-don")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @PostMapping("/tao-hoa-don")
    public ResponseEntity<?> addHoaDon(@RequestBody HoaDonEntity hoadon) {

        hoaDonService.addHoaDon(hoadon);

        return ResponseEntity.ok("Tao thanh cong!");
    }

    @PostMapping("/test-hoadon")
    public ResponseEntity<?> testHoaDon(
            @RequestBody HoaDonEntity hoaDon
    ) {

        return ResponseEntity.ok(hoaDon);
    }

    @GetMapping("/get-hoa-don/{maPhieuDat}")
    public ResponseEntity<?> getHoaDon(@PathVariable String maPhieuDat) {

        var result = hoaDonService.getHoaDon(maPhieuDat);

        if (result != null) {
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.badRequest().body(null);
    }
}
