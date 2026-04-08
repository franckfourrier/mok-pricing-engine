/*À exécuter UNE FOIS :*/
ALTER DATABASE pricing_db SET timezone TO 'UTC';

-- V2__ledger_indexes.sql
CREATE INDEX IF NOT EXISTS idx_ledger_acc_curr_time_desc
    ON ledger_entries (account_code, currency, occurred_at DESC);

CREATE INDEX IF NOT EXISTS idx_ledger_covering
    ON ledger_entries (account_code, currency, occurred_at DESC)
    INCLUDE (amount, direction);

CREATE INDEX idx_ledger_acc_time_xaf
    ON ledger_entries (account_code, occurred_at DESC)
    WHERE currency = 'XAF';

-- Quand ton système grossit : Partitioning recommandé
CREATE TABLE ledger_entries_2026 PARTITION OF ledger_entries
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');