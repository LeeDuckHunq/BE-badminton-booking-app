package com.example.alopoapi.controller;

import com.example.alopoapi.dto.QRResponse;
import com.example.alopoapi.repository.QRRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class QRController {

    @Autowired
    private QRRepo qrRepo;

    @GetMapping("/getQR/{receiver}")
    public ResponseEntity<?> getQRCode(
            @PathVariable String receiver
    ) {

        var qrOptional = qrRepo.findById(receiver);

        if (qrOptional.isEmpty()) {
            return ResponseEntity
                    .status(404)
                    .body("Khong tim thay QR");
        }

        var qr = qrOptional.get();

        QRResponse response = new QRResponse(
                qr.getReceiver(),
                qr.getQRcode()
        );

        return ResponseEntity.ok(response);
    }
}