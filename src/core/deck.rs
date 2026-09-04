use sqlx::sqlite::SqlitePool;

#[derive(sqlx::FromRow)]
struct Deck {
    pub id: i64,
    user_id: i64,
    name: String
}


pub async fn create_deck(pool: &SqlitePool, user_id: i64, deck_name: &String) -> Result<bool, sqlx::Error> {
    let query = sqlx::query("INSERT INTO decks (user_id, name) VALUES (?,?)")
        .bind(user_id)
        .bind(deck_name);
    let result =   query.execute(pool).await?;
    Ok(result.rows_affected() > 0)
}