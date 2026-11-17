package com.example.alopoapi.config;

import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Hash mọi mật khẩu còn lưu dạng plaintext (dữ liệu cũ từ MySQL) ngay khi ứng dụng khởi động.
 * Idempotent: mật khẩu đã là BCrypt sẽ được bỏ qua.
 */
@Configuration
@ConditionalOnProperty(name = "app.migrations.enabled", havingValue = "true", matchIfMissing = true)
public class PasswordMigrationConfig {

    private static final Logger log = LoggerFactory.getLogger(PasswordMigrationConfig.class);

    @Bean
    CommandLineRunner hashPlaintextPasswords(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            int migrated = 0;
            for (UserEntity user : userRepo.findAll()) {
                String stored = user.getPassword();
                if (stored == null || stored.isEmpty() || isBcrypt(stored)) {
                    continue;
                }
                user.setPassword(passwordEncoder.encode(stored));
                userRepo.save(user);
                migrated++;
            }
            if (migrated > 0) {
                log.info("Hashed {} plaintext password(s)", migrated);
            }
        };
    }

    static boolean isBcrypt(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
