-- fix_existing_data.sql
-- Uruchom ten skrypt raz na istniejącej bazie, żeby:
--  1. Zmienić nazwy gatunków z polskich na angielskie (StoryGraph style)
--  2. Book-genre links (ksiazka_gatunek) zostają nienaruszone – używają ID

SET search_path TO biblioteka, public;

-- 1. Zmień polskie nazwy gatunków na angielskie (StoryGraph)
UPDATE gatunki SET nazwa = 'Romance'            WHERE nazwa = 'Romans';
UPDATE gatunki SET nazwa = 'Crime'              WHERE nazwa = 'Kryminał';
UPDATE gatunki SET nazwa = 'Speculative Fiction' WHERE nazwa = 'Spekulatywna';
UPDATE gatunki SET nazwa = 'Historical Fiction' WHERE nazwa = 'Historyczna';
UPDATE gatunki SET nazwa = 'Adventure'          WHERE nazwa = 'Przygodowa';
UPDATE gatunki SET nazwa = 'Drama'              WHERE nazwa = 'Dramat';
-- Fantasy, Science Fiction, Horror, Humor – already English, no change needed

-- Podsumowanie
SELECT id_gatunku, nazwa FROM gatunki ORDER BY id_gatunku;



