CREATE TABLE decks (
    id INTEGER PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    name VARCHAR(100),
    CONSTRAINT UC_Person UNIQUE (user_id, name)
)