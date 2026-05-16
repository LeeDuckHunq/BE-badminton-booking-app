package com.example.alopoapi.repository;

import com.example.alopoapi.entity.SanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SanRepo extends JpaRepository<SanEntity, String> {
}
