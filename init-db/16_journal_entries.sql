-- 16_journal_entries.sql - Automatic reading activity log entries
CREATE TABLE wpisy_dziennika (
    id_wpisu       SERIAL PRIMARY KEY,
    id_reading_log INT REFERENCES reading_log(id_reading_log) ON DELETE CASCADE,
    entry_type     VARCHAR(20) NOT NULL CHECK (entry_type IN ('STATUS_CHANGE', 'PROGRESS_UPDATE')),
    status         VARCHAR(20) CHECK (status IN ('TO_READ', 'READING', 'FINISHED', 'DNF')),
    strony_laczne  INT DEFAULT 0,
    data_wpisu     DATE DEFAULT CURRENT_DATE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_wpisy_dziennika_modtime
    BEFORE UPDATE ON wpisy_dziennika
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
