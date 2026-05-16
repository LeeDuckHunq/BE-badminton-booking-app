package com.example.alopoapi.controller;

import com.example.alopoapi.service.PhieuDatSanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/phieu-dat-san")
public class PhieuDatSanController {

    @Autowired
    private PhieuDatSanService phieuDatSanService;

    @GetMapping("/theo-ngay")
    public ResponseEntity<?> getTheoNgay(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate ngay
    ) {

        return ResponseEntity.ok(
                phieuDatSanService.getPhieuDatTheoNgay(ngay)
        );
    }
}
