package com.example.alopoapi.controller;

import com.example.alopoapi.dto.TopCumSanDTO;
import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.service.CumSanService;
import com.example.alopoapi.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cumsan")
public class CumSanController {

    @Autowired
    private CumSanService cumSanService;

    @Autowired
    private ThongKeService thongKeService;

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

    /*
    @PostMapping("/create-cum-san")
    public ResponseEntity<?> createCumSan(@RequestBody CumSanEntity cumsan) {

        return ;
    }

     */
}
