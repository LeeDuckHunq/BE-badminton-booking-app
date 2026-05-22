package com.example.alopoapi.controller;

import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.service.SanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/san")
public class SanController {

    @Autowired
    private SanService sanService;

    @GetMapping("/get-san/{maCumSan}")
    public ResponseEntity<?> getSanTheoCum(@PathVariable String maCumSan) {

        return ResponseEntity.ok(sanService.getSanTheoCum(maCumSan));

    }

    @GetMapping("/get-so-luong-san")
    public ResponseEntity<?> getSoLuongSan() {

        return ResponseEntity.ok(sanService.getSoLuongSan());

    }

    @GetMapping("/san-info/{maSan}")
    public ResponseEntity<?> getSanInfo(@PathVariable String maSan) {

        return ResponseEntity.ok(sanService.getSanInfo(maSan));
    }

    @GetMapping("/get-all-san/{maCumSan}")
    public ResponseEntity<?> getAllSanTheoCumSan(@PathVariable String maCumSan) {

        return ResponseEntity.ok(sanService.getAllSanTheoCum(maCumSan));
    }

    @PostMapping("/add-san")
    public ResponseEntity<?> addNewSan(@RequestBody SanEntity newSan) {

        var result = sanService.addNewSan(newSan);

        if (result) {
            return ResponseEntity.ok("Add new San successfully");
        }

        return ResponseEntity.badRequest().body("Failed!");
    }

    @PutMapping("/edit-san")
    public ResponseEntity<?> editSan(@RequestBody SanEntity san) {

        var result = sanService.editSan(san);

        if (result) {
            return ResponseEntity.ok("Edit San successfully");
        }

        return ResponseEntity.badRequest().body("Failed!");
    }

    @DeleteMapping("/delete-san/{maSan}")
    public ResponseEntity<?> deleteSan(@PathVariable String maSan) {

        var result = sanService.deleteSan(maSan);

        if (result) {
            return ResponseEntity.ok("Delete San successfully");
        }

        return ResponseEntity.badRequest().body("Failed!");
    }
}
