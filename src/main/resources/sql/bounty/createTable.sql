CREATE TABLE IF NOT EXISTS bounties (
    issuer TEXT NOT NULL,
    wanted TEXT NOT NULL,
    bounty DOUBLE NOT NULL,
    PRIMARY KEY(wanted, issuer)
)