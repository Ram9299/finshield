-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- USERS
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL UNIQUE,
  country VARCHAR(2) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ACCOUNTS
CREATE TABLE accounts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id),
  balance NUMERIC(18,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);

-- TRANSACTIONS
CREATE TABLE transactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  account_id UUID NOT NULL REFERENCES accounts(id),
  amount NUMERIC(18,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  txn_type VARCHAR(10) NOT NULL,      -- DEBIT / CREDIT
  country VARCHAR(2) NOT NULL,
  device_id VARCHAR(120) NOT NULL,
  ip_address VARCHAR(45) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_txn_account_created_at ON transactions(account_id, created_at DESC);
CREATE INDEX idx_txn_country_created_at ON transactions(country, created_at DESC);

-- FRAUD SIGNALS (rule hits)
CREATE TABLE fraud_signals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  transaction_id UUID NOT NULL REFERENCES transactions(id),
  signal_type VARCHAR(40) NOT NULL,   -- RAPID_TXN / GEO_ANOMALY / etc.
  risk_weight INT NOT NULL,
  details TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_signals_txn_id ON fraud_signals(transaction_id);

-- RISK SCORES (final decision per transaction)
CREATE TABLE risk_scores (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  transaction_id UUID NOT NULL UNIQUE REFERENCES transactions(id),
  total_score INT NOT NULL,
  decision VARCHAR(10) NOT NULL,      -- SAFE / REVIEW / BLOCK
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_risk_scores_created_at ON risk_scores(created_at DESC);

-- ALERTS
CREATE TABLE alerts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  account_id UUID NOT NULL REFERENCES accounts(id),
  transaction_id UUID NOT NULL REFERENCES transactions(id),
  alert_type VARCHAR(40) NOT NULL DEFAULT 'FRAUD_SUSPECTED',
  status VARCHAR(10) NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_alerts_status_created_at ON alerts(status, created_at DESC);
CREATE INDEX idx_alerts_account_id ON alerts(account_id);