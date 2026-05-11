# Projekt: Aplikacja Webowa (Spring Boot) - System Zarządzania Informacjami

## Model Domenowy i Baza Danych (56p. / 30p. techniczne)
- [x] Implementacja co najmniej 3 klas domeny — `Book`, `Author`, `Reader`, `Genre`, `Series`, `Publisher`, `Review`, `ReadingLog`, `JournalEntry`, `ReadingGoal`.
- [x] Relacja **kompozycji** pomiędzy co najmniej 2 klasami — `Book → Author` (ManyToOne), `Book → genres` (ManyToMany), `Review → Book`, `ReadingLog → Book → Reader`.
- [x] Zróżnicowane typy pól w klasach:
    - [x] `String` — `title`, `username`, `email`, `content`, `mood` itp.
    - [x] `int` / `Integer` — `pageCount`, `publicationYear`, `pagesRead`.
    - [x] `double` / `Double` — `Review.rating` (ocena 0.25–5.00).
    - [x] Co najmniej jedno pole typu `Date` — `Reader.birthDate` (LocalDate), `ReadingLog.startDate`, `ReadingLog.finishDate`, `JournalEntry.entryDate`.
- [x] Ograniczenia i walidacja pól — `nullable`, `length`, `unique`, `@Column` constraints w modelach JPA.
- [x] Baza danych:
    - [x] Konfiguracja co najmniej 2 tabel z relacją — 14+ tabel z kluczami obcymi (PostgreSQL, pliki `init-db/`).
    - [ ] **Kluczowe:** Zapis do bazy danych dopiero przy wylogowaniu/wygaśnięciu sesji — ❌ NIE ZROBIONE (architektura bezstanowa – JWT, zapis natychmiastowy przez REST).

## Bezpieczeństwo i Użytkownicy
- [x] Spring Security:
    - [x] Integracja z bazą danych — `ReaderRepository` + `BCryptPasswordEncoder` + `JwtFilter`.
    - [x] Formularz logowania — `LoginView.vue` + POST `/api/v1/auth/login`.
    - [x] Rejestracja dla niezalogowanych użytkowników — `RegisterView.vue` + `/api/v1/auth/register`.
- [ ] Role użytkowników (minimum 3 role, w tym `ADMIN`):
    - [x] `USER` — dostęp do własnych danych (półki, dziennik, recenzje).
    - [x] `MODERATOR` — zarządzanie recenzjami/zgłoszeniami (`ModeratorController`).
    - [ ] `ADMIN` — ❌ NIE ZROBIONE (brak trzeciej roli z pełnym dostępem).
- [ ] Zarządzanie rolami przez Administratora — ❌ NIE ZROBIONE (brak roli ADMIN).
- [x] Wyświetlanie listy użytkowników — `ReadersView.vue` + `GET /api/v1/readers` (dostępne dla MODERATOR).

## Interfejs Użytkownika (Thymeleaf & WCAG)
- [ ] Strona powitalna (dostępna dla każdego) — ❌ NIE ZROBIONE (brak dedykowanej strony powitalnej `/`).
- [ ] Zastosowanie co najmniej 5 różnych znaczników Thymeleaf — ❌ NIE DOTYCZY (frontend w Vue.js, nie Thymeleaf).
- [ ] Formularze z walidacją (minimum 3 różne elementy formularza) — ⚠️ Formularze istnieją w Vue.js, ale nie w Thymeleaf.
- [ ] Zapewnienie dostępności zgodnie z **WCAG 2.1** — ❌ NIE ZROBIONE.

## Funkcjonalności CRUD i Logika Biznesowa (20p.)
- [x] Pełny CRUD dla każdej z 3 klas domeny — `BookController`, `AuthorController`, `SeriesController`, `ReviewController`, `ReadingLogController`, `JournalEntryController`.
- [x] Edycja danych na "danych bieżących" — PUT endpointy dla wszystkich zasobów.
- [ ] **Walidacja formularzy:** Implementacja 6 różnych reguł walidacyjnych — ⚠️ Częściowe (walidacja w SQL + frontend Vue, ale brak 6 jawnych `@Valid` / `@NotBlank` / `@Size` / `@Min` itp. w Spring).
- [ ] **Sortowanie:**
    - [ ] Sortowanie w obu kierunkach (ASC/DESC) — ❌ NIE ZROBIONE.
    - [ ] Obsługa 3 różnych kryteriów sortowania — ❌ NIE ZROBIONE.
    - [ ] Zapamiętywanie kierunku i kryterium sortowania w sesji/ciasteczkach — ❌ NIE ZROBIONE.
- [ ] **Filtrowanie:**
    - [ ] Filtrowanie po dacie (od aktualnej wzwyż) — ❌ NIE ZROBIONE.
    - [x] Filtrowanie po wybranym polu domeny — `BooksView.vue` filtruje po gatunku, autorze, serii, nastroju.
- [ ] **Udostępnianie:**
    - [ ] Współdzielenie danych między użytkownikami (wskazanie usera lub link) — ❌ NIE ZROBIONE.
    - [ ] Wyświetlanie informacji z linku dla niezalogowanych — ❌ NIE ZROBIONE.
- [ ] Specjalny widok "Przypomnienia" zaraz po zalogowaniu (jeśli data przypomnienia == dziś) — ❌ NIE ZROBIONE.

## Warstwa Techniczna (REST & Sesja)
- [x] Implementacja Kontrolerów MVC — 10+ kontrolerów REST (`@RestController`).
- [x] **Usługa REST (Server):** Wystawienie endpointów dla zasobów — pełne API pod `/api/v1/` (Swagger: `/swagger-ui.html`).
- [ ] **Klient REST:** Aplikacja jako konsument usługi REST — ⚠️ Frontend Vue.js (axios) konsumuje REST, ale brak Javowego klienta (`RestTemplate` / `WebClient`).
- [ ] Obsługa sesji (przechowywanie tymczasowych danych przed zapisem do DB) — ❌ NIE ZROBIONE (bezstanowe JWT).
- [ ] Obsługa ciasteczek — ❌ NIE ZROBIONE.

## Dokumentacja i Prezentacja (60p.)
- [ ] Stworzenie dokumentacji technicznej:
    - [ ] Odniesienie do każdego punktowanego elementu.
    - [ ] Wskazanie konkretnych plików/katalogów realizujących wymogi.
    - [ ] Opis co udało się zrealizować, a czego nie.
- [ ] Przygotowanie prezentacji na forum grupy.
- [ ] Kamienie milowe (prezentacje postępów):
    - [ ] Prezentacja I (20-30% kodu).
    - [ ] Prezentacja II (60-70% kodu).

---
**Termin oddania:** Ostatnie planowe zajęcia.

---

## ⚠️ Pozostało do zrobienia (priorytety)
1. **Rola ADMIN** — dodać trzecią rolę z pełnym dostępem i panel zarządzania rolami użytkowników.
2. **Sortowanie** — ASC/DESC po 3 kryteriach (tytuł, rok, ocena) + zapamiętywanie w `localStorage` / ciasteczkach.
3. **Filtrowanie po dacie** — np. książki dodane od/do daty w `BooksView`.
4. **Strona powitalna** — widok dla niezalogowanych pod `/` z opisem aplikacji.
5. **6 reguł walidacyjnych w Spring** — jawnie: `@NotBlank`, `@Size`, `@Min`, `@Max`, `@Email`, `@Past` z `@Valid` w kontrolerach.
6. **Udostępnianie** — publiczny link do profilu czytelnika lub jego listy książek (dostępny bez logowania).
7. **Widok Przypomnień** — po zalogowaniu, jeśli masz książkę "TO_READ" czekającą od ponad X dni.
8. **Dokumentacja techniczna** — plik `DOKUMENTACJA.md` z odniesieniami do konkretnych plików.
