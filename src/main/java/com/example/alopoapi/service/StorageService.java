package com.example.alopoapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
public class StorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final String bucket;
    private final HttpClient httpClient;

    @Autowired
    public StorageService(
            @Value("${app.supabase.url:}") String supabaseUrl,
            @Value("${app.supabase.service-role-key:}") String serviceRoleKey,
            @Value("${app.supabase.storage-bucket:images}") String bucket
    ) {
        this(supabaseUrl, serviceRoleKey, bucket, HttpClient.newHttpClient());
    }

    StorageService(String supabaseUrl, String serviceRoleKey, String bucket, HttpClient httpClient) {
        this.supabaseUrl = supabaseUrl == null ? "" : supabaseUrl.replaceAll("/+$", "");
        this.serviceRoleKey = serviceRoleKey == null ? "" : serviceRoleKey.trim();
        this.bucket = bucket == null ? "images" : bucket.trim();
        this.httpClient = httpClient;
    }

    public String uploadPaymentProof(MultipartFile file, String username) {
        ensureConfigured();
        byte[] content = readAndValidate(file);
        ImageType imageType = detectImageType(content);
        if (imageType == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chỉ hỗ trợ ảnh JPEG, PNG hoặc WebP"
            );
        }

        LocalDate today = LocalDate.now();
        String safeUsername = sanitizeUsername(username);
        String objectPath = "payment-proofs/%d/%02d/%s-%s.%s".formatted(
                today.getYear(),
                today.getMonthValue(),
                safeUsername,
                UUID.randomUUID(),
                imageType.extension()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("%s/storage/v1/object/%s/%s".formatted(supabaseUrl, bucket, objectPath)))
                .header("apikey", serviceRoleKey)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("Content-Type", imageType.contentType())
                .header("x-upsert", "false")
                .POST(HttpRequest.BodyPublishers.ofByteArray(content))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Supabase Storage từ chối tải ảnh (HTTP " + response.statusCode() + ")"
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Tải ảnh bị gián đoạn", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không thể kết nối Supabase Storage", exception);
        }

        return "%s/storage/v1/object/public/%s/%s".formatted(supabaseUrl, bucket, objectPath);
    }

    /**
     * Chỉ chấp nhận URL bill do chính API này tải lên cho đúng người dùng:
     * {supabase}/storage/v1/object/public/{bucket}/payment-proofs/yyyy/MM/{username}-{uuid}.{ext}
     */
    public boolean isOwnedPaymentProofUrl(String url, String username) {
        if (url == null || supabaseUrl.isBlank() || bucket.isBlank()) {
            return false;
        }
        String prefix = "%s/storage/v1/object/public/%s/payment-proofs/".formatted(supabaseUrl, bucket);
        if (!url.startsWith(prefix)) {
            return false;
        }
        String rest = url.substring(prefix.length());
        String[] parts = rest.split("/");
        if (parts.length != 3 || !parts[0].matches("\\d{4}") || !parts[1].matches("\\d{2}")) {
            return false;
        }
        return parts[2].matches(java.util.regex.Pattern.quote(sanitizeUsername(username))
                + "-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)");
    }

    private void ensureConfigured() {
        if (supabaseUrl.isBlank() || serviceRoleKey.isBlank() || bucket.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Supabase Storage chưa được cấu hình");
        }
    }

    private byte[] readAndValidate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng chọn ảnh cần tải lên");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Ảnh không được vượt quá 5 MB");
        }
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể đọc tệp ảnh", exception);
        }
    }

    private ImageType detectImageType(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff) {
            return new ImageType("jpg", "image/jpeg");
        }
        if (bytes.length >= 8
                && (bytes[0] & 0xff) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4e
                && bytes[3] == 0x47
                && bytes[4] == 0x0d
                && bytes[5] == 0x0a
                && bytes[6] == 0x1a
                && bytes[7] == 0x0a) {
            return new ImageType("png", "image/png");
        }
        if (bytes.length >= 12
                && bytes[0] == 'R'
                && bytes[1] == 'I'
                && bytes[2] == 'F'
                && bytes[3] == 'F'
                && bytes[8] == 'W'
                && bytes[9] == 'E'
                && bytes[10] == 'B'
                && bytes[11] == 'P') {
            return new ImageType("webp", "image/webp");
        }
        return null;
    }

    private String sanitizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return "user";
        }
        String sanitized = username.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "-");
        return sanitized.isBlank() ? "user" : sanitized;
    }

    private record ImageType(String extension, String contentType) {
    }
}
