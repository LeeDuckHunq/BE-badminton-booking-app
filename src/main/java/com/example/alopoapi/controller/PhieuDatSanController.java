package com.example.alopoapi.controller;

import com.example.alopoapi.dto.request.CreatePhieuDatRequest;
import com.example.alopoapi.service.PhieuDatSanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/create")
    public ResponseEntity<?>
    createPhieuDat(

            @RequestBody
            CreatePhieuDatRequest request
    ) {

        return ResponseEntity.ok(
                phieuDatSanService
                        .createPhieuDat(
                                request
                        )
        );
    }

    @GetMapping("/get-theo-user/{username}")
    public ResponseEntity<?> getPhieuDatForUser(@PathVariable String username) {

        return ResponseEntity.ok(phieuDatSanService.getPhieuDatForUser(username));
    }
}
