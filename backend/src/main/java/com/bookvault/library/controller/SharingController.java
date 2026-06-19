package com.bookvault.library.controller;

import com.bookvault.library.service.SharingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SharingController {

    private final SharingService sharingService;


    @PostMapping("/api/v1/reading-log/share")
    public ResponseEntity<?> generateShareToken(HttpServletRequest request) {
        return sharingService.generateShareToken(request);
    }


    @DeleteMapping("/api/v1/reading-log/share")
    public ResponseEntity<?> revokeShareToken(HttpServletRequest request) {
        return sharingService.revokeShareToken(request);
    }


    @GetMapping("/api/v1/shared/{token}")
    public ResponseEntity<?> getSharedShelf(@PathVariable String token) {
        return sharingService.getSharedShelf(token);
    }
}
