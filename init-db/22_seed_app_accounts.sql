-- 22_seed_app_accounts.sql
-- Test application accounts.
-- Passwords are BCrypt hashes of the plaintext shown in the comment.
--
-- Admin accounts     -> admin123
-- Moderator accounts -> moderator123

SET search_path TO biblioteka, public;

DO $$
DECLARE
v_hash_admin     TEXT := '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'; -- sprawdzić, czy na pewno admin123
    v_hash_moderator TEXT := '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGnyHidN6BC9j6cOW/SS'; -- sprawdzić, czy na pewno moderator123
BEGIN
    -- Admin accounts
INSERT INTO czytelnicy (username, email, password_hash, role, narodowosc)
VALUES
    ('admin',  'admin@bookvault.com',  v_hash_admin, 'ADMIN', 'Poland'),
    ('admin2', 'admin2@bookvault.com', v_hash_admin, 'ADMIN', 'Poland')
    ON CONFLICT (email) DO UPDATE
                               SET
                                   username = EXCLUDED.username,
                               password_hash = EXCLUDED.password_hash,
                               role = EXCLUDED.role,
                               narodowosc = EXCLUDED.narodowosc,
                               updated_at = CURRENT_TIMESTAMP;

-- Moderator accounts
INSERT INTO czytelnicy (username, email, password_hash, role, narodowosc)
VALUES
    ('moderator',  'moderator@bookvault.com',  v_hash_moderator, 'MODERATOR', 'Poland'),
    ('moderator2', 'moderator2@bookvault.com', v_hash_moderator, 'MODERATOR', 'Poland'),
    ('moderator3', 'moderator3@bookvault.com', v_hash_moderator, 'MODERATOR', 'Poland'),
    ('moderator4', 'moderator4@bookvault.com', v_hash_moderator, 'MODERATOR', 'Poland'),
    ('moderator5', 'moderator5@bookvault.com', v_hash_moderator, 'MODERATOR', 'Poland')
    ON CONFLICT (email) DO UPDATE
                               SET
                                   username = EXCLUDED.username,
                               password_hash = EXCLUDED.password_hash,
                               role = EXCLUDED.role,
                               narodowosc = EXCLUDED.narodowosc,
                               updated_at = CURRENT_TIMESTAMP;

RAISE NOTICE 'App accounts seeded or updated: 2 admins, 5 moderators.';
END $$;