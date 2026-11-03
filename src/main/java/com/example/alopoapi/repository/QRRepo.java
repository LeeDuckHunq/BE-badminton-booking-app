package com.example.alopoapi.repository;

import com.example.alopoapi.entity.bank.QR;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QRRepo extends MongoRepository<QR, String> {
}
