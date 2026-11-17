package com.example.alopoapi.controller;

import com.example.alopoapi.dto.TopCumSanDTO;
import com.example.alopoapi.service.CumSanService;
import com.example.alopoapi.service.ThongKeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API đọc công khai cho app khách hàng.
 * Tạo / sửa / ẩn cụm sân được thực hiện qua /management/venues (yêu cầu quyền ADMIN/OWNER).
 */
@RestController
@RequestMapping("/cumsan")
public class CumSanController {

    private final CumSanService cumSanService;
    private final ThongKeService thongKeService;

    public CumSanController(CumSanService cumSanService, ThongKeService thongKeService) {
        this.cumSanService = cumSanService;
        this.thongKeService = thongKeService;
    }

    @GetMapping("/{amount}")
    public ResponseEntity<?> getCumSan(@PathVariable int amount) {
        return ResponseEntity.ok(cumSanService.getCumSan(amount));
    }

    @GetMapping("/cum-san-info/{maCumSan}")
    public ResponseEntity<?> getCumSanInfo(@PathVariable String maCumSan) {
        return ResponseEntity.ok(cumSanService.getCumSanInfo(maCumSan));
    }

    @GetMapping("/top-10-cum-san")
    public ResponseEntity<List<TopCumSanDTO>> getTop10CumSan() {
        return ResponseEntity.ok(thongKeService.getTop10CumSanDatNhieuNhat());
    }
}
