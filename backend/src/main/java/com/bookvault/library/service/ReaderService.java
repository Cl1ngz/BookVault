package com.bookvault.library.service;

import com.bookvault.library.dto.ReaderDto;
import com.bookvault.library.model.Reader;
import com.bookvault.library.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReaderService {

    private final ReaderRepository readerRepository;

    public List<Reader> getAllReaders(String username, String nationality) {
        if (username != null && !username.isBlank()) {
            return readerRepository.findByUsernameContainingIgnoreCase(username.trim());
        }
        if (nationality != null && !nationality.isBlank()) {
            return readerRepository.findByNationalityContainingIgnoreCase(nationality.trim());
        }
        return readerRepository.findAll();
    }

    public ResponseEntity<?> getReaderById(Integer id, Authentication authentication) {
        return readerRepository.findById(id)
                .map(reader -> {
                    boolean isOwner = reader.getEmail() != null
                            && reader.getEmail().equalsIgnoreCase(authentication.getName());

                    boolean isModerator = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR"));

                    if (!isOwner && !isModerator) {
                        return ResponseEntity.status(403).body("You can only access your own profile");
                    }

                    ReaderDto readerDto = new ReaderDto(
                            reader.getId(),
                            reader.getUsername(),
                            reader.getEmail(),
                            reader.getBirthDate(),
                            reader.getNationality(),
                            reader.getRole()
                    );

                    return ResponseEntity.ok(readerDto);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

