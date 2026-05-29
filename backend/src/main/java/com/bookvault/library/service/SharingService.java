package com.bookvault.library.service;

import com.bookvault.library.config.JwtUtils;
import com.bookvault.library.model.Reader;
import com.bookvault.library.model.ReadingLog;
import com.bookvault.library.repository.ReaderRepository;
import com.bookvault.library.repository.ReadingLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SharingService {

    private final ReaderRepository readerRepository;
    private final ReadingLogRepository readingLogRepository;
    private final JwtUtils jwtUtils;

    public Reader authenticate(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7);
        if (!jwtUtils.isValid(token)) return null;
        return readerRepository.findByEmail(jwtUtils.extractEmail(token)).orElse(null);
    }

    public ResponseEntity<?> generateShareToken(HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        if (reader.getShareToken() == null) {
            reader.setShareToken(UUID.randomUUID().toString());
            readerRepository.save(reader);
        }

        String url = "/shared/" + reader.getShareToken();
        return ResponseEntity.ok(Map.of(
                "shareToken", reader.getShareToken(),
                "url", url,
                "owner", reader.getUsername()
        ));
    }

    public ResponseEntity<?> revokeShareToken(HttpServletRequest request) {
        Reader reader = authenticate(request);
        if (reader == null) return ResponseEntity.status(401).body("Unauthorized");

        reader.setShareToken(null);
        readerRepository.save(reader);
        return ResponseEntity.ok(Map.of("message", "Share link revoked"));
    }

    public ResponseEntity<?> getSharedShelf(String token) {
        Reader owner = readerRepository.findByShareToken(token).orElse(null);
        if (owner == null) return ResponseEntity.status(404).body("Share link not found or has been revoked");

        List<ReadingLog> shelf = readingLogRepository.findByReader_Id(owner.getId());
        return ResponseEntity.ok(Map.of(
                "owner", owner.getUsername(),
                "shelf", shelf
        ));
    }
}

