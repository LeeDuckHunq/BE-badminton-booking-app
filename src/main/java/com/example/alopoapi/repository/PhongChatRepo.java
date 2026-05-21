package com.example.alopoapi.repository;

import com.example.alopoapi.entity.chat.PhongChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhongChatRepo extends JpaRepository<PhongChatEntity, String> {
}
