package com.bookvault.library.controller;

import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ReaderRepository readerRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest().body("username, email and password are required");
        }

        Reader reader = new Reader();
        reader.setUsername(username);
        reader.setEmail(email);
        reader.setPasswordHash(password);

        return ResponseEntity.ok(readerRepository.save(reader));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        // Use fully qualified type to avoid clash with java.io.Reader
        com.bookvault.library.model.Reader found = readerRepository.findByEmail(email).orElse(null);
        if (found == null || !found.getPasswordHash().equals(password)) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        return ResponseEntity.ok(Map.of(
                "id", found.getId(),
                "username", found.getUsername(),
                "email", found.getEmail()
        ));
    }
}
