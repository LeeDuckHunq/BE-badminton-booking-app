package com.example.alopoapi.repository;

import com.example.alopoapi.entity.chat.ThanhVienPhongChatEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThanhVienPhongChatRepo
        extends MongoRepository<ThanhVienPhongChatEntity, String> {

    List<ThanhVienPhongChatEntity>
    findByMaPhongChat(String maPhongChat);

    List<ThanhVienPhongChatEntity>
    findByUsername(String username);
}
