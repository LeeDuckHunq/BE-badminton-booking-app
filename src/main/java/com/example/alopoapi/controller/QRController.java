package com.example.alopoapi.controller;

import com.example.alopoapi.entity.QR;
import com.example.alopoapi.repository.QRRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QRController {

    @Autowired
    private QRRepo qrRepo;

    @GetMapping("/getQR/{receiver}")
    public ResponseEntity<?> getQRCode(@PathVariable String receiver) {

        var qr = qrRepo.findById(receiver);

        return ResponseEntity.ok(qr.get().getQRcode());
    }
}
