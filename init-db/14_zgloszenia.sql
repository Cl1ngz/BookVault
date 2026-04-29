CREATE TABLE zgloszenia_recenzji
(
    id_zgloszenia SERIAL PRIMARY KEY,
    id_recenzji   INT REFERENCES recenzje (id_recenzji) ON DELETE CASCADE,
    reporter_type VARCHAR(10) NOT NULL CHECK (reporter_type IN ('reader', 'author')),
    reporter_id   INT         NOT NULL,
    reason        TEXT        NOT NULL,
    status        VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'resolved', 'dismissed')),
    created_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_zgloszenia_modtime
    BEFORE UPDATE
    ON zgloszenia_recenzji
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
