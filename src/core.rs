// This file must not be aware of anything from Axum, as axum is a router dealing with network, while core.rs is about functionalities
use sqlx::sqlite::SqlitePool;

pub async fn connect() -> Result<SqlitePool, Box<dyn std::error::Error>> {

    // Connections pool to db
    let pool = SqlitePool::connect("sqlite://anki.db?mode=rwc").await?; // pool is pointer to the pool
    sqlx::migrate!().run(&pool).await?; // question mark returns error if there is a failure
    Ok(pool)
}

pub async fn count_users(pool: &SqlitePool) -> Result<u16, sqlx::Error> {
    let query = sqlx::query_scalar("SELECT COUNT(*) FROM users");
    let outcome: u16 = query.fetch_one(pool).await?;
    Ok(outcome)
}