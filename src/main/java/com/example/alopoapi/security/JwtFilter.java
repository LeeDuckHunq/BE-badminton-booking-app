package com.example.alopoapi.security;

import com.example.alopoapi.entity.UserEntity;
import com.example.alopoapi.repository.UserRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final List<String> VALID_ROLES = List.of("ROLE_USER", "ROLE_OWNER", "ROLE_ADMIN");

    private final JwtService jwtService;
    private final UserRepo userRepo;

    public JwtFilter(JwtService jwtService, UserRepo userRepo) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = authorization.substring(7);
            if (jwtService.validateToken(token)) {
                // Đọc lại tài khoản từ DB: tài khoản bị khoá hoặc bị đổi quyền có hiệu lực ngay,
                // không phải chờ token hết hạn.
                Optional<UserEntity> user = userRepo.findByUsernameIgnoreCase(jwtService.extractUsername(token));
                if (user.isPresent() && user.get().isActive()) {
                    String role = normalizeRole(user.get().getRole());
                    if (role != null) {
                        var authentication = new UsernamePasswordAuthenticationToken(
                                user.get().getUsername(),
                                null,
                                List.of(new SimpleGrantedAuthority(role))
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return VALID_ROLES.contains(normalized) ? normalized : null;
    }
}
