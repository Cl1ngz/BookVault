package com.bookvault.library.controller;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ReaderRepository readerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email    = body.get("email");
        String password = body.get("password");

        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest().body("username, email and password are required");
        }
        if (readerRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        Reader reader = new Reader();
        reader.setUsername(username);
        reader.setEmail(email);
        reader.setPasswordHash(passwordEncoder.encode(password)); // BCrypt!

        readerRepository.save(reader);
        String token = jwtUtils.generateToken(reader.getEmail(), reader.getId(), reader.getUsername(), reader.getRole());
        return ResponseEntity.ok(Map.of("token", token, "username", username, "role", reader.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String password = body.get("password");

        com.bookvault.library.model.Reader found = readerRepository.findByEmail(email).orElse(null);
        if (found == null || !passwordEncoder.matches(password, found.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        String token = jwtUtils.generateToken(found.getEmail(), found.getId(), found.getUsername(), found.getRole());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "id", found.getId(),
                "username", found.getUsername(),
                "email", found.getEmail(),
                "role", found.getRole()
        ));
    }
}
