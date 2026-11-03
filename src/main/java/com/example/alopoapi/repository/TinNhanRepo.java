package com.example.alopoapi.repository;

import com.example.alopoapi.entity.chat.TinNhanEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TinNhanRepo extends MongoRepository<TinNhanEntity, String> {

    List<TinNhanEntity>
    findByMaPhongChatOrderByThoiGianGuiAsc(String maPhongChat);
}
