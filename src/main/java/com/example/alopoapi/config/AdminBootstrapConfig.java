package com.example.alopoapi.config;

import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.repository.UserRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrapConfig {

    @Bean
    CommandLineRunner bootstrapAdmin(
            UserRepo userRepo,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username:}") String username,
            @Value("${app.admin.password:}") String password,
            @Value("${app.admin.full-name:SmashZone Administrator}") String fullName
    ) {
        return args -> {
            if (username == null || username.isBlank() || password == null || password.isBlank()) {
                return;
            }

            UserEntity admin = userRepo.findByUsernameIgnoreCase(username.trim())
                    .orElseGet(UserEntity::new);
            admin.setUsername(username.trim());
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRole("ROLE_ADMIN");
            admin.setFullName(fullName);
            admin.setActive(true);
            userRepo.save(admin);
        };
    }
}
