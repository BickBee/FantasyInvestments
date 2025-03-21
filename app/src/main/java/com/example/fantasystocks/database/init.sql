CREATE TABLE leagues (
    league_id   SERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE
);

CREATE TABLE user_league (
    uid             uuid,
    league_id       INT,
    cash            DECIMAL(10,2),
    initial_value   DECIMAL(10,2),
    PRIMARY KEY (uid, league_id),
    FOREIGN KEY (uid) REFERENCES auth.users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (league_id) REFERENCES league(league_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE historical_portfolio_value (
    uid         uuid,
    league_id   INT,
    timestamp   TIMESTAMP NOT NULL,
    value       DECIMAL(10,2),
    PRIMARY KEY (uid, league_id, timestamp),
    FOREIGN KEY (uid) REFERENCES auth.users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (league_id) REFERENCES league(league_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE stock (
    stock_id    SERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    ticker      VARCHAR(10) NOT NULL
);

CREATE TABLE transactions (
    txn_id      SERIAL PRIMARY KEY,
    uid         uuid,
    league_id   INT,
    stock_id    INT,
    action      TEXT CHECK (action IN ('BUY', 'SELL')) NOT NULL,
    quantity    DECIMAL(20,8) NOT NULL,
    price       DECIMAL(10,2) NOT NULL,
    timestamp   TIMESTAMP NOT NULL,
    FOREIGN KEY (uid) REFERENCES auth.users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (league_id) REFERENCES league(league_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (stock_id) REFERENCES stock(stock_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE portfolio (
    uid         uuid,
    league_id   INT,
    stock_id    INT,
    quantity    DECIMAL(20,8),
    PRIMARY KEY (uid, league_id, stock_id),
    FOREIGN KEY (uid) REFERENCES auth.users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (league_id) REFERENCES league(league_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (stock_id) REFERENCES stock(stock_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE historical_stock_price (
    stock_id    INT,
    timestamp   TIMESTAMP NOT NULL,
    open        DECIMAL(10,2) NOT NULL,
    close       DECIMAL(10,2) NOT NULL,
    high        DECIMAL(10,2) NOT NULL,
    low         DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (stock_id, timestamp),
    FOREIGN KEY (stock_id) REFERENCES stock(stock_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE user_information (
    uid uuid PRIMARY KEY REFERENCES auth.users(id),
    username VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE user_settings (
    uid uuid PRIMARY KEY REFERENCES auth.users(id),
    dark_mode BOOLEAN DEFAULT false,
    notification_enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_friends (
    user_id uuid REFERENCES auth.users(id) ON DELETE CASCADE,
    friend_id uuid REFERENCES auth.users(id) ON DELETE CASCADE,
    status TEXT CHECK (status IN ('PENDING', 'ACCEPTED')) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT no_self_friendship CHECK (user_id != friend_id)
);

-- Create trigger to update timestamps
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_user_friends_timestamp
BEFORE UPDATE ON user_friends
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- View for player portfolios
CREATE VIEW player_portfolio_view AS
SELECT
    ul.uid,
    ul.league_id,
    ui.username,
    ul.cash,
    ul.initial_value,
    p.stock_id,
    p.quantity,
    s.name AS stock_name,
    s.ticker AS stock_ticker
FROM
    user_league ul
JOIN
    user_information ui ON ul.uid = ui.uid
LEFT JOIN
    portfolio p ON ul.uid = p.uid AND ul.league_id = p.league_id
LEFT JOIN
    stock s ON p.stock_id = s.stock_id;

-- View for transactions
CREATE VIEW transaction_view AS
SELECT
    txn_id,
    uid,
    league_id,
    s.stock_id,
    s.name stock_name,
    action,
    quantity,
    price,
    timestamp
FROM
    transactions t
JOIN
    stock s ON t.stock_id = s.stock_id;