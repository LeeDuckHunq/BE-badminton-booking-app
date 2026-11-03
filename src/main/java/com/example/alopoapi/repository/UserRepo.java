package com.example.alopoapi.repository;

import com.example.alopoapi.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepo extends MongoRepository<UserEntity, String> {
    Optional<UserEntity> findByUsernameIgnoreCase(String username);
}
