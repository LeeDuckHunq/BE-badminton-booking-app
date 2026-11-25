package com.example.alopoapi.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageServiceTests {

    @Test
    void rejectsUploadWhenStorageIsNotConfigured() {
        StorageService service = new StorageService("", "", "images", HttpClient.newHttpClient());
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "bill.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a}
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.uploadPaymentProof(image, "customer")
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void rejectsFilesWhoseContentIsNotASupportedImage() {
        StorageService service = new StorageService(
                "https://example.supabase.co",
                "service-role-key",
                "images",
                HttpClient.newHttpClient()
        );
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "bill.jpg",
                "image/jpeg",
                "not-an-image".getBytes()
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.uploadPaymentProof(textFile, "customer")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void acceptsOnlyPaymentProofUrlsUploadedForTheSameUser() {
        StorageService service = new StorageService(
                "https://example.supabase.co/",
                "service-role-key",
                "images",
                HttpClient.newHttpClient()
        );
        String base = "https://example.supabase.co/storage/v1/object/public/images/payment-proofs/2026/11/";
        String uuid = "123e4567-e89b-12d3-a456-426614174000";

        org.junit.jupiter.api.Assertions.assertTrue(
                service.isOwnedPaymentProofUrl(base + "customer-" + uuid + ".png", "customer"));
        org.junit.jupiter.api.Assertions.assertFalse(
                service.isOwnedPaymentProofUrl(base + "other-" + uuid + ".png", "customer"));
        org.junit.jupiter.api.Assertions.assertFalse(
                service.isOwnedPaymentProofUrl("https://evil.example/" + "customer-" + uuid + ".png", "customer"));
        org.junit.jupiter.api.Assertions.assertFalse(
                service.isOwnedPaymentProofUrl(base + "../customer-" + uuid + ".png", "customer"));
    }

    @Test
    void newSecretKeyIsSentOnlyAsApiKey() {
        var headers = StorageService.authHeaders("sb_secret_abc123");
        assertEquals("sb_secret_abc123", headers.get("apikey"));
        assertEquals(false, headers.containsKey("Authorization"));
    }

    @Test
    void legacyServiceRoleJwtIsAlsoSentAsBearer() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoic2VydmljZV9yb2xlIn0.sig";
        var headers = StorageService.authHeaders(jwt);
        assertEquals(jwt, headers.get("apikey"));
        assertEquals("Bearer " + jwt, headers.get("Authorization"));
    }
}
