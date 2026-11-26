package com.example.alopoapi.controller;

import com.example.alopoapi.security.AccessGuard;
import com.example.alopoapi.service.BanBeService;
import com.example.alopoapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final BanBeService banBeService;

    public UserController(UserService userService, BanBeService banBeService) {
        this.banBeService = banBeService;
        this.userService = userService;
    }

    /** Công khai: chỉ trả về một con số thống kê. */
    @GetMapping("/get-so-luong-user")
    public ResponseEntity<?> getSoLuongUser() {
        return ResponseEntity.ok(userService.getSoLuongUser());
    }

    @GetMapping("/get-user-info/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable String username, Authentication authentication) {
        AccessGuard.requireSelfOrAdmin(authentication, username);
        return ResponseEntity.ok(userService.requireUser(username));
    }

    /**
     * Danh bạ chat: admin thấy tất cả; người khác chỉ thấy bạn bè và người đã từng nhắn
     * (không còn liệt kê toàn bộ người dùng — muốn nhắn người mới thì kết bạn qua /ban-be).
     */
    @GetMapping("/get-all-user")
    public ResponseEntity<?> getAllUser(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return ResponseEntity.ok(admin ? userService.getAllUser() : banBeService.contacts(authentication.getName()));
    }
}
