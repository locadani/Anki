use sqlx::sqlite::SqlitePool;

#[derive(serde::Serialize)]
#[derive(sqlx::FromRow)]
pub struct Deck {
    pub id: i64,
    user_id: i64,
    pub name: String
}


pub async fn create_deck(pool: &SqlitePool, user_id: i64, deck_name: &String) -> Result<bool, sqlx::Error> {
    let query = sqlx::query("INSERT INTO decks (user_id, name) VALUES (?,?)")
        .bind(user_id)
        .bind(deck_name);
    let result =   query.execute(pool).await?;
    Ok(result.rows_affected() > 0)
}

pub async fn list_decks(pool: &SqlitePool, user_id: i64) -> Result<Vec<Deck>, sqlx::Error> {
    let query = sqlx::query_as::<_, Deck>("SELECT * FROM decks WHERE user_id = ?")
        .bind(user_id);
    let result =   query.fetch_all(pool).await?;
    Ok(result)
}