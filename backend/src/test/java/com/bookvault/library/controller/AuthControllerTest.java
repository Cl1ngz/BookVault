package com.bookvault.library.controller;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ReaderRepository readerRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtils jwtUtils;

    @BeforeEach
    void cleanup() {
        readerRepository.findByEmail("authtest@test.com")
                .ifPresent(readerRepository::delete);
        readerRepository.findByEmail("banned_auth@test.com")
                .ifPresent(readerRepository::delete);
    }


    @Test
    void register_success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"username":"authtest","email":"authtest@test.com","password":"pass123"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("authtest"));
    }

    @Test
    void register_duplicateEmail_returns400() throws Exception {
        // Insert a reader first
        Reader r = new Reader();
        r.setUsername("existing");
        r.setEmail("authtest@test.com");
        r.setPasswordHash(passwordEncoder.encode("pass123"));
        readerRepository.save(r);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"username":"other","email":"authtest@test.com","password":"pass123"}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already in use"));
    }

    @Test
    void register_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"email":"authtest@test.com"}
                """))
                .andExpect(status().isBadRequest());
    }


    @Test
    void login_success() throws Exception {
        Reader r = new Reader();
        r.setUsername("authtest");
        r.setEmail("authtest@test.com");
        r.setPasswordHash(passwordEncoder.encode("pass123"));
        readerRepository.save(r);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"email":"authtest@test.com","password":"pass123"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("authtest"))
                .andExpect(jsonPath("$.email").value("authtest@test.com"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        Reader r = new Reader();
        r.setUsername("authtest");
        r.setEmail("authtest@test.com");
        r.setPasswordHash(passwordEncoder.encode("pass123"));
        readerRepository.save(r);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"email":"authtest@test.com","password":"wrongpassword"}
                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password"));
    }

    @Test
    void login_unknownEmail_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"email":"nobody@nowhere.com","password":"pass123"}
                """))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // JWT token validation tests
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void jwtUtils_generatedTokenIsValid() {
        Reader r = new Reader();
        r.setUsername("authtest");
        r.setEmail("authtest@test.com");
        r.setId(1);
        r.setRole("USER");
        String token = jwtUtils.generateToken(r.getEmail(), r.getId(), r.getUsername(), r.getRole());
        assert jwtUtils.isValid(token);
        assert "authtest@test.com".equals(jwtUtils.extractEmail(token));
        assert "USER".equals(jwtUtils.extractRole(token));
        assert "authtest".equals(jwtUtils.extractUsername(token));
    }

    @Test
    void jwtUtils_tamperedToken_isInvalid() {
        String token = jwtUtils.generateToken("test@test.com", 1, "test", "USER");
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + ".INVALIDSIGNATURE";
        assert !jwtUtils.isValid(tampered);
    }

    @Test
    void jwtUtils_garbageToken_isInvalid() {
        assert !jwtUtils.isValid("this.is.garbage");
        assert !jwtUtils.isValid("");
        assert !jwtUtils.isValid("noteven.dotted");
    }

    @Test
    void jwtUtils_tokenSignedWithWrongSecret_isInvalid() {
        String wrongSecret = "completelywrongsecretkeythatisnotthesame!!";
        String forged = io.jsonwebtoken.Jwts.builder()
                .subject("attacker@test.com")
                .claim("id", 999)
                .claim("username", "attacker")
                .claim("role", "ADMIN")
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 86400000L))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        wrongSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
        assert !jwtUtils.isValid(forged);
    }

    @Test
    void protectedEndpoint_withValidAdminToken_returns200() throws Exception {
        Reader r = new Reader();
        r.setUsername("authtest");
        r.setEmail("authtest@test.com");
        r.setPasswordHash(passwordEncoder.encode("pass123"));
        r.setRole("ADMIN");
        r = readerRepository.save(r);
        String token = jwtUtils.generateToken(r.getEmail(), r.getId(), r.getUsername(), r.getRole());

        // ADMIN accessing the admin endpoint should get 200
        mockMvc.perform(get("/api/v1/admin/readers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withNoToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/readers"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_withTamperedToken_returns403() throws Exception {
        Reader r = new Reader();
        r.setUsername("authtest");
        r.setEmail("authtest@test.com");
        r.setPasswordHash(passwordEncoder.encode("pass123"));
        r.setRole("ADMIN");
        r = readerRepository.save(r);
        String token = jwtUtils.generateToken(r.getEmail(), r.getId(), r.getUsername(), r.getRole());
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + ".TAMPERED";

        mockMvc.perform(get("/api/v1/admin/readers")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_bannedUser_returns403() throws Exception {
        Reader r = new Reader();
        r.setUsername("banneduser");
        r.setEmail("banned_auth@test.com");
        r.setPasswordHash(passwordEncoder.encode("pass123"));
        r.setRole("USER");
        r.setBannedUntil(java.time.LocalDate.now().plusDays(3));
        readerRepository.save(r);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"email":"banned_auth@test.com","password":"pass123"}
                """))
                .andExpect(status().isForbidden());
    }
}
