package com.bookvault.library.controller;

import com.bookvault.library.model.Reader;
import com.bookvault.library.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/readers")
    public ResponseEntity<List<Reader>> getAllReaders() {
        return ResponseEntity.ok(adminService.getAllReaders());
    }

    @PutMapping("/readers/{id}/role")
    public ResponseEntity<?> setRole(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        return adminService.setRole(id, body, authentication);
    }

    @PutMapping("/readers/{id}/ban")
    public ResponseEntity<?> banReader(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body
    ) {
        return adminService.banReader(id, body);
    }

    @DeleteMapping("/readers/{id}")
    public ResponseEntity<?> deleteReader(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        return adminService.deleteReader(id, authentication);
    }
}
