# Biblioteka - Docker & Spring Boot

Projekt systemu bibliotecznego z bazą danych PostgreSQL w pełni skonfigurowaną przez Docker Compose.

## Szybki Start

Wymagany zainstalowany Docker i Docker Compose.

1. Sklonuj repozytorium.
2. W głównym folderze wykonaj komendę:

   docker-compose up -d db

Baza posiada automatycznie inicjalizowany schemat biblioteka.

    Skrypty init: 00-10 (struktura), 11 (dane testowe), 12 (logika), 13 (indeksy).

    Kaskady: Implementacja ON DELETE CASCADE dla spójności danych.

    Audyt: Automatyczne kolumny created_at i updated_at obsługiwane przez triggery.
