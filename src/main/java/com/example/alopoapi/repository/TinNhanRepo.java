package com.example.alopoapi.repository;

import com.example.alopoapi.entity.chat.TinNhanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TinNhanRepo extends JpaRepository<TinNhanEntity, String> {

    List<TinNhanEntity>
    findByMaPhongChatOrderByThoiGianGuiAsc(String maPhongChat);
}
