package com.example.alopoapi.repository;

import com.example.alopoapi.entity.chat.PhongChatEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhongChatRepo extends MongoRepository<PhongChatEntity, String> {
}
