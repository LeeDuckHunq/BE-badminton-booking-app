package com.example.alopoapi.repository;

import com.example.alopoapi.entity.SanEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SanRepo extends JpaRepository<SanEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s
        FROM SanEntity s
        WHERE s.maSan = :maSan
    """)
    Optional<SanEntity> findByIdForUpdate(
            @Param("maSan") String maSan
    );
}
