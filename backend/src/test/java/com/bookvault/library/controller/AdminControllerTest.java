package com.bookvault.library.controller;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//  Security tests for AdminController.

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ReaderRepository readerRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtUtils jwtUtils;

    private String adminToken;
    private String secondAdminToken;
    private String userToken;
    private String moderatorToken;

    private Reader adminReader;
    private Reader secondAdminReader;
    private Reader userReader;
    private Reader moderatorReader;

    @BeforeEach
    void setup() {
        adminReader = readerRepository.findByEmail("admin_test@test.com").orElseGet(() -> {
            Reader r = new Reader();
            r.setUsername("admintest");
            r.setEmail("admin_test@test.com");
            r.setPasswordHash(passwordEncoder.encode("pass123"));
            r.setRole("ADMIN");
            return readerRepository.save(r);
        });
        if (!"ADMIN".equals(adminReader.getRole())) {
            adminReader.setRole("ADMIN");
            adminReader = readerRepository.save(adminReader);
        }
        adminToken = jwtUtils.generateToken(
                adminReader.getEmail(), adminReader.getId(),
                adminReader.getUsername(), adminReader.getRole());

        secondAdminReader = readerRepository.findByEmail("admin2_test@test.com").orElseGet(() -> {
            Reader r = new Reader();
            r.setUsername("admintest2");
            r.setEmail("admin2_test@test.com");
            r.setPasswordHash(passwordEncoder.encode("pass123"));
            r.setRole("ADMIN");
            return readerRepository.save(r);
        });
        if (!"ADMIN".equals(secondAdminReader.getRole())) {
            secondAdminReader.setRole("ADMIN");
            secondAdminReader = readerRepository.save(secondAdminReader);
        }
        secondAdminToken = jwtUtils.generateToken(
                secondAdminReader.getEmail(), secondAdminReader.getId(),
                secondAdminReader.getUsername(), secondAdminReader.getRole());

        userReader = readerRepository.findByEmail("user_test@test.com").orElseGet(() -> {
            Reader r = new Reader();
            r.setUsername("usertest");
            r.setEmail("user_test@test.com");
            r.setPasswordHash(passwordEncoder.encode("pass123"));
            r.setRole("USER");
            return readerRepository.save(r);
        });
        if (!"USER".equals(userReader.getRole())) {
            userReader.setRole("USER");
            userReader = readerRepository.save(userReader);
        }
        userToken = jwtUtils.generateToken(
                userReader.getEmail(), userReader.getId(),
                userReader.getUsername(), userReader.getRole());

        moderatorReader = readerRepository.findByEmail("mod_test@test.com").orElseGet(() -> {
            Reader r = new Reader();
            r.setUsername("moderatortest");
            r.setEmail("mod_test@test.com");
            r.setPasswordHash(passwordEncoder.encode("pass123"));
            r.setRole("MODERATOR");
            return readerRepository.save(r);
        });
        if (!"MODERATOR".equals(moderatorReader.getRole())) {
            moderatorReader.setRole("MODERATOR");
            moderatorReader = readerRepository.save(moderatorReader);
        }
        moderatorToken = jwtUtils.generateToken(
                moderatorReader.getEmail(), moderatorReader.getId(),
                moderatorReader.getUsername(), moderatorReader.getRole());
    }

    // Access control – unauthenticated

    @Test
    @Order(1)
    void getAllReaders_noToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/readers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(2)
    void deleteReader_noToken_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/readers/" + userReader.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void setRole_noToken_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/admin/readers/" + userReader.getId() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"MODERATOR\"}"))
                .andExpect(status().isForbidden());
    }

    // Access control – USER role

    @Test
    @Order(4)
    void getAllReaders_userToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/readers")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void deleteReader_userToken_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/readers/" + userReader.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    void setRole_userToken_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/admin/readers/" + userReader.getId() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content("{\"role\":\"MODERATOR\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    void banReader_userToken_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/admin/readers/" + userReader.getId() + "/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content("{\"days\":3}"))
                .andExpect(status().isForbidden());
    }

    // Access control – MODERATOR role

    @Test
    @Order(8)
    void getAllReaders_moderatorToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/readers")
                        .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(9)
    void deleteReader_moderatorToken_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/readers/" + userReader.getId())
                        .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isForbidden());
    }

    // ADMIN – allowed operations

    @Test
    @Order(10)
    void getAllReaders_adminToken_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/readers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(11)
    void setRole_adminChangesUserToModerator_returns200() throws Exception {
        mockMvc.perform(put("/api/v1/admin/readers/" + userReader.getId() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"role\":\"MODERATOR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MODERATOR"));

        // Reset role back so other tests are not affected
        userReader = readerRepository.findById(userReader.getId()).orElseThrow();
        userReader.setRole("USER");
        readerRepository.save(userReader);
    }

    @Test
    @Order(12)
    void banReader_adminBansUser_returns200() throws Exception {
        mockMvc.perform(put("/api/v1/admin/readers/" + userReader.getId() + "/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"days\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bannedUntil").isNotEmpty());

        // Unban so other tests are not affected
        mockMvc.perform(put("/api/v1/admin/readers/" + userReader.getId() + "/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"days\":0}"))
                .andExpect(status().isOk());
    }

    // SECURITY: Admin cannot delete themselves

    @Test
    @Order(20)
    void deleteReader_adminDeletesOwnAccount_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/readers/" + adminReader.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    // SECURITY: Admin cannot delete another admin

    @Test
    @Order(21)
    void deleteReader_adminDeletesAnotherAdmin_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/readers/" + secondAdminReader.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    // SECURITY: Admin can delete a regular user

    @Test
    @Order(22)
    void deleteReader_adminDeletesUser_returns200() throws Exception {
        // Create a temporary user to delete
        Reader tmp = new Reader();
        tmp.setUsername("tmpuser_todelete");
        tmp.setEmail("tmp_delete@test.com");
        tmp.setPasswordHash(passwordEncoder.encode("pass123"));
        tmp.setRole("USER");
        tmp = readerRepository.save(tmp);

        mockMvc.perform(delete("/api/v1/admin/readers/" + tmp.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User tmpuser_todelete deleted"));
    }

    @Test
    @Order(23)
    void deleteReader_adminDeletesNonExistentUser_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/readers/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // SECURITY: Role change protection — admin cannot change another admin's role

    @Test
    @Order(30)
    void setRole_adminChangesAnotherAdminRole_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/admin/readers/" + secondAdminReader.getId() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"role\":\"USER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(31)
    void setRole_invalidRole_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/admin/readers/" + userReader.getId() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"role\":\"SUPERUSER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(32)
    void setRole_missingRoleField_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/admin/readers/" + userReader.getId() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // SECURITY: Admin cannot ban another admin

    @Test
    @Order(40)
    void banReader_adminBansAnotherAdmin_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/admin/readers/" + secondAdminReader.getId() + "/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"days\":7}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(41)
    void banReader_negativeDays_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/admin/readers/" + userReader.getId() + "/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"days\":-1}"))
                .andExpect(status().isBadRequest());
    }

    // SECURITY: Token theft / cookie hijacking — tampered tokens are rejected

    @Test
    @Order(50)
    void adminEndpoint_withTamperedToken_returns403() throws Exception {
        // Take a valid token and corrupt the signature portion
        String[] parts = adminToken.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".invalidsignature_TAMPERED";

        mockMvc.perform(get("/api/v1/admin/readers")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(51)
    void adminEndpoint_withRandomGibberishToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/readers")
                        .header("Authorization", "Bearer this.is.notavalidjwt"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(52)
    void adminEndpoint_withEmptyBearerToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/readers")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isForbidden());
    }

    /**
     * Privilege escalation: attacker takes their own USER token, decodes the payload,
     * re-encodes with "role":"ADMIN" but signed with a WRONG secret.
     * The JwtFilter should reject this token because the signature is invalid.
     * <p>
     * We simulate this by building a token with a different secret.
     */
    @Test
    @Order(53)
    void adminEndpoint_privilegeEscalation_forgedAdminRoleInToken_returns403() throws Exception {
        // Build a forged token manually: valid structure but signed with a different secret
        String wrongSecret = "thisisacompletlywrongsecretkeythatdoesnotmatch!!";

        String forgedToken = io.jsonwebtoken.Jwts.builder()
                .subject(userReader.getEmail())
                .claim("id", userReader.getId())
                .claim("username", userReader.getUsername())
                .claim("role", "ADMIN")          // escalated role
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 86400000L))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        wrongSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();

        mockMvc.perform(get("/api/v1/admin/readers")
                        .header("Authorization", "Bearer " + forgedToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Cookie / session token theft simulation:
     * An attacker steals a valid MODERATOR token and tries to use it
     * against the ADMIN-only endpoint. Even though the token is valid,
     * the role is insufficient.
     */
    @Test
    @Order(54)
    void adminEndpoint_stolenModeratorToken_returns403() throws Exception {
        // moderatorToken is a real, signed, valid token — just wrong role
        mockMvc.perform(get("/api/v1/admin/readers")
                        .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Stolen user token used against delete endpoint.
     */
    @Test
    @Order(55)
    void deleteEndpoint_stolenUserToken_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/readers/" + userReader.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Replaying the same admin token with a malformed payload (truncated) is rejected.
     */
    @Test
    @Order(56)
    void adminEndpoint_truncatedToken_returns403() throws Exception {
        String truncated = adminToken.substring(0, adminToken.length() / 2);
        mockMvc.perform(get("/api/v1/admin/readers")
                        .header("Authorization", "Bearer " + truncated))
                .andExpect(status().isForbidden());
    }

    /**
     * Token with only the header present (no payload or signature).
     */
    @Test
    @Order(57)
    void adminEndpoint_headerOnlyToken_returns403() throws Exception {
        String headerOnly = adminToken.split("\\.")[0];
        mockMvc.perform(get("/api/v1/admin/readers")
                        .header("Authorization", "Bearer " + headerOnly))
                .andExpect(status().isForbidden());
    }
}


