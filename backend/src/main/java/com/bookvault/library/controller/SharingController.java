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

    /**
     * POST /api/v1/reading-log/share
     * Generates (or returns existing) share token for the current reader's shelf.
     * Returns the public URL that anyone can visit without logging in.
     */
    @PostMapping("/api/v1/reading-log/share")
    public ResponseEntity<?> generateShareToken(HttpServletRequest request) {
        return sharingService.generateShareToken(request);
    }

    /**
     * DELETE /api/v1/reading-log/share
     * Revokes the share token so the shelf is no longer public.
     */
    @DeleteMapping("/api/v1/reading-log/share")
    public ResponseEntity<?> revokeShareToken(HttpServletRequest request) {
        return sharingService.revokeShareToken(request);
    }

    /**
     * GET /api/v1/shared/{token}
     * Public endpoint – no authentication required.
     * Returns the shelf (reading log entries) of the reader who owns this token.
     */
    @GetMapping("/api/v1/shared/{token}")
    public ResponseEntity<?> getSharedShelf(@PathVariable String token) {
        return sharingService.getSharedShelf(token);
    }
}
