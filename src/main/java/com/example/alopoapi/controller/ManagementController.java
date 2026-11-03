package com.example.alopoapi.controller;

import com.example.alopoapi.entity.CumSanEntity;
import com.example.alopoapi.entity.SanEntity;
import com.example.alopoapi.service.ManagementService;
import com.example.alopoapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/management")
public class ManagementController {

    private final ManagementService managementService;
    private final UserService userService;

    public ManagementController(ManagementService managementService, UserService userService) {
        this.managementService = managementService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        return ResponseEntity.ok(managementService.currentUser(authentication));
    }

    @GetMapping("/overview")
    public ResponseEntity<?> overview(Authentication authentication) {
        return ResponseEntity.ok(managementService.overview(authentication));
    }

    @GetMapping("/venues")
    public ResponseEntity<?> venues(Authentication authentication) {
        return ResponseEntity.ok(managementService.venues(authentication));
    }

    @PostMapping("/venues")
    public ResponseEntity<?> createVenue(@RequestBody CumSanEntity request, Authentication authentication) {
        return ResponseEntity.ok(managementService.saveVenue(request, authentication, null));
    }

    @PutMapping("/venues/{id}")
    public ResponseEntity<?> updateVenue(@PathVariable String id, @RequestBody CumSanEntity request, Authentication authentication) {
        return ResponseEntity.ok(managementService.saveVenue(request, authentication, id));
    }

    @DeleteMapping("/venues/{id}")
    public ResponseEntity<?> deleteVenue(@PathVariable String id, Authentication authentication) {
        managementService.deleteVenue(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courts")
    public ResponseEntity<?> courts(Authentication authentication) {
        return ResponseEntity.ok(managementService.courts(authentication));
    }

    @PostMapping("/courts")
    public ResponseEntity<?> createCourt(@RequestBody SanEntity request, Authentication authentication) {
        return ResponseEntity.ok(managementService.saveCourt(request, authentication, null));
    }

    @PutMapping("/courts/{id}")
    public ResponseEntity<?> updateCourt(@PathVariable String id, @RequestBody SanEntity request, Authentication authentication) {
        return ResponseEntity.ok(managementService.saveCourt(request, authentication, id));
    }

    @DeleteMapping("/courts/{id}")
    public ResponseEntity<?> deleteCourt(@PathVariable String id, Authentication authentication) {
        managementService.deleteCourt(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> bookings(Authentication authentication) {
        return ResponseEntity.ok(managementService.bookings(authentication));
    }

    @PatchMapping("/bookings/{id}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(managementService.updateBookingStatus(id, request.get("status"), authentication));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<?> users() {
        return ResponseEntity.ok(userService.getManagedUsers());
    }

    @PostMapping("/admin/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(userService.createManagedUser(request));
    }

    @PatchMapping("/admin/users/{username}/role")
    public ResponseEntity<?> updateRole(@PathVariable String username, @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(userService.updateRole(username, request.get("role")));
    }

    @PatchMapping("/admin/users/{username}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String username, @RequestBody Map<String, Boolean> request) {
        return ResponseEntity.ok(userService.updateStatus(username, Boolean.TRUE.equals(request.get("active"))));
    }
}
