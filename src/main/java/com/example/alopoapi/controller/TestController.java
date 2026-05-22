package com.example.alopoapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@RestController
public class TestController {
    @GetMapping("/test-time")
    public ResponseEntity<?> testTime() {

        Map<String, Object> map = new HashMap<>();

        map.put("LocalDateTime", LocalDateTime.now());
        map.put("ZonedDateTime", ZonedDateTime.now());
        map.put("TimeZone",
                TimeZone.getDefault().getID());

        return ResponseEntity.ok(map);
    }
}
