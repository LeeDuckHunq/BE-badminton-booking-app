package com.example.alopoapi.service;

import com.example.alopoapi.dto.UserDTO;
import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.repository.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean register(Map<String, String> newUser) {
        String username = clean(newUser.get("username"));
        String password = newUser.get("password");
        if (username == null || password == null || password.length() < 6 || userRepo.findByUsernameIgnoreCase(username).isPresent()) {
            return false;
        }

        userRepo.save(new UserEntity(
                username,
                passwordEncoder.encode(password),
                "ROLE_USER",
                clean(newUser.get("fullName")),
                clean(newUser.get("email")),
                clean(newUser.get("phoneNumber"))
        ));
        return true;
    }

    public Optional<UserEntity> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }

        Optional<UserEntity> result = userRepo.findByUsernameIgnoreCase(username.trim());
        if (result.isEmpty() || !result.get().isActive()) {
            return Optional.empty();
        }

        UserEntity user = result.get();
        String stored = user.getPassword();
        boolean encoded = stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
        boolean valid = encoded ? passwordEncoder.matches(password, stored) : password.equals(stored);

        if (!valid) {
            return Optional.empty();
        }

        if (!encoded) {
            user.setPassword(passwordEncoder.encode(password));
            userRepo.save(user);
        }
        user.setRole(normalizeRole(user.getRole()));
        return Optional.of(user);
    }

    public UserEntity createManagedUser(Map<String, String> request) {
        String username = clean(request.get("username"));
        String password = request.get("password");
        if (username == null || password == null || password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username và mật khẩu tối thiểu 8 ký tự là bắt buộc");
        }
        if (userRepo.findByUsernameIgnoreCase(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại");
        }

        UserEntity user = new UserEntity(
                username,
                passwordEncoder.encode(password),
                normalizeRole(request.get("role")),
                clean(request.get("fullName")),
                clean(request.get("email")),
                clean(request.get("phoneNumber"))
        );
        return userRepo.save(user);
    }

    public UserEntity updateRole(String username, String role) {
        UserEntity user = requireUser(username);
        user.setRole(normalizeRole(role));
        return userRepo.save(user);
    }

    public UserEntity updateStatus(String username, boolean active) {
        UserEntity user = requireUser(username);
        user.setActive(active);
        return userRepo.save(user);
    }

    public UserEntity requireUser(String username) {
        return userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
    }

    public int getSoLuongUser() {
        return (int) userRepo.count();
    }

    public Optional<UserEntity> userInfo(String username) {
        return userRepo.findByUsernameIgnoreCase(username);
    }

    public List<UserDTO> getAllUser() {
        return userRepo.findAll().stream()
                .map(user -> new UserDTO(user.getUsername(), user.getFullName()))
                .toList();
    }

    public List<UserEntity> getManagedUsers() {
        return userRepo.findAll();
    }

    public String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        if (!List.of("ROLE_USER", "ROLE_OWNER", "ROLE_ADMIN").contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role không hợp lệ");
        }
        return normalized;
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
