package com.bookvault.library.service;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ReaderRepository readerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public ResponseEntity<?> register(Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        if (isBlank(username) || isBlank(email) || isBlank(password)) {
            return ResponseEntity.badRequest().body("username, email and password are required");
        }

        if (readerRepository.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        Reader reader = new Reader();
        reader.setUsername(username.trim());
        reader.setEmail(email.trim().toLowerCase());
        reader.setPasswordHash(passwordEncoder.encode(password));
        reader.setRole("USER");

        Reader saved = readerRepository.save(reader);

        String token = jwtUtils.generateToken(saved.getEmail(), saved.getId(), saved.getUsername(), saved.getRole());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "id", saved.getId(),
                "username", saved.getUsername(),
                "email", saved.getEmail(),
                "role", saved.getRole()
        ));
    }

    public ResponseEntity<?> login(Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (isBlank(email) || isBlank(password)) {
            return ResponseEntity.badRequest().body("email and password are required");
        }

        Reader found = readerRepository.findByEmail(email.trim().toLowerCase()).orElse(null);

        if (found == null || !passwordEncoder.matches(password, found.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        if (found.isBanned()) {
            return ResponseEntity.status(403).body("Account banned until " + found.getBannedUntil());
        }

        String role = found.getRole() != null ? found.getRole() : "USER";

        String token = jwtUtils.generateToken(found.getEmail(), found.getId(), found.getUsername(), role);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "id", found.getId(),
                "username", found.getUsername(),
                "email", found.getEmail(),
                "role", role
        ));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

