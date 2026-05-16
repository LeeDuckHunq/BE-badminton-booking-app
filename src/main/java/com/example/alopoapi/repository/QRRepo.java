package com.example.alopoapi.repository;

import com.example.alopoapi.entity.QR;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QRRepo extends JpaRepository<QR, String> {
}
