use sqlx::sqlite::SqlitePool;

#[derive(sqlx::FromRow)]
pub struct User {
    pub id: i64,
    username: String,
    pub password_hash: String
}

pub async fn find_by_username(pool: &SqlitePool, username: &str) -> Result<Option<User>, sqlx::Error> {
    let query = sqlx::query_as::<_, User>("SELECT * FROM users WHERE username = ?").bind(username);
    let outcome: Option<User> = query.fetch_optional(pool).await?;
    Ok(outcome)
}