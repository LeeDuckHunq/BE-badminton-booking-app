package com.example.alopoapi.controller;

import com.example.alopoapi.service.DemoDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Chỉ admin (đường dẫn nằm trong /management/admin/**): sinh / xem / xoá dữ liệu demo. */
@RestController
@RequestMapping("/management/admin/demo-data")
public class DemoDataController {

    private final DemoDataService service;

    public DemoDataController(DemoDataService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(service.status());
    }

    @PostMapping
    public ResponseEntity<?> generate() {
        return ResponseEntity.ok(service.generate());
    }

    @DeleteMapping
    public ResponseEntity<?> clear() {
        return ResponseEntity.ok(service.clear());
    }
}
