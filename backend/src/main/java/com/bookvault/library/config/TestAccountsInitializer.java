package com.bookvault.library.config;

import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
public class TestAccountsInitializer implements CommandLineRunner {

    private final ReaderRepository readerRepository;
    private final PasswordEncoder passwordEncoder;

    public TestAccountsInitializer(ReaderRepository readerRepository,
                                   PasswordEncoder passwordEncoder) {
        this.readerRepository = readerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureAccount("admin", "admin@bookvault.com", "admin123", "ADMIN");
        ensureAccount("admin2", "admin2@bookvault.com", "admin123", "ADMIN");

        ensureAccount("moderator", "moderator@bookvault.com", "moderator123", "MODERATOR");
        ensureAccount("moderator2", "moderator2@bookvault.com", "moderator123", "MODERATOR");
        ensureAccount("moderator3", "moderator3@bookvault.com", "moderator123", "MODERATOR");
        ensureAccount("moderator4", "moderator4@bookvault.com", "moderator123", "MODERATOR");
        ensureAccount("moderator5", "moderator5@bookvault.com", "moderator123", "MODERATOR");
    }

    private void ensureAccount(String username, String email, String plainPassword, String role) {
        Reader reader = readerRepository.findByEmailIgnoreCase(email)
                .orElseGet(Reader::new);

        reader.setUsername(username);
        reader.setEmail(email);
        reader.setRole(role);
        reader.setNationality("Poland");

        String currentHash = reader.getPasswordHash();

        if (currentHash == null || !passwordEncoder.matches(plainPassword, currentHash)) {
            reader.setPasswordHash(passwordEncoder.encode(plainPassword));
        }

        readerRepository.save(reader);
    }
}