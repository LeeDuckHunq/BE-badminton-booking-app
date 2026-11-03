package com.example.alopoapi.repository;

import com.example.alopoapi.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<UserEntity, String> {
}
