package com.example.alopoapi.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

public final class AccessGuard {

    private AccessGuard() {
    }

    public static String username(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập");
        }
        return authentication.getName();
    }

    public static boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    public static boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "ROLE_ADMIN");
    }

    public static boolean isSelf(Authentication authentication, String username) {
        return username != null && username(authentication).equalsIgnoreCase(username);
    }

    /** Cho phép chính chủ tài khoản hoặc ADMIN. */
    public static void requireSelfOrAdmin(Authentication authentication, String username) {
        if (!isSelf(authentication, username) && !isAdmin(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập dữ liệu này");
        }
    }

    /** Chỉ cho phép chính chủ tài khoản. */
    public static void requireSelf(Authentication authentication, String username) {
        if (!isSelf(authentication, username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập dữ liệu này");
        }
    }
}
