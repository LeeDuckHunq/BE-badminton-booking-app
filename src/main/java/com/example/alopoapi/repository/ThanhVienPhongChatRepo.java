package com.example.alopoapi.repository;

import com.example.alopoapi.entity.chat.ThanhVienPhongChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThanhVienPhongChatRepo
        extends JpaRepository<ThanhVienPhongChatEntity, Integer> {

    @Query(value = """
            SELECT t1.maPhongChat
            FROM ThanhVienPhongChat t1
            JOIN ThanhVienPhongChat t2
            ON t1.maPhongChat = t2.maPhongChat
            WHERE t1.username = :u1
            AND t2.username = :u2
            LIMIT 1
            """, nativeQuery = true)
    String findPrivateRoom(
            @Param("u1") String u1,
            @Param("u2") String u2
    );

    List<ThanhVienPhongChatEntity>
    findByMaPhongChat(String maPhongChat);
}
