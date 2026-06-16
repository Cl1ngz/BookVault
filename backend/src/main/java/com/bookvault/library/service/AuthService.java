package com.bookvault.library.service;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.dto.RegisterRequestDto;
import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ReaderRepository readerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Transactional
    public ResponseEntity<?> register(RegisterRequestDto request) {
        
        if (readerRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        readerRepository.registerReader(username, email, encodedPassword);

        Reader saved = readerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Rejestracja się nie powiodła"));

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