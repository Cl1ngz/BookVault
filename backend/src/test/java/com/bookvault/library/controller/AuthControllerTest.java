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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    }

    // ── REGISTER ─────────────────────────────────────────────────

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

    // ── LOGIN ─────────────────────────────────────────────────────

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
}
