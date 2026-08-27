use axum::{
    extract::State,
    http::StatusCode,
    Json,
    routing::get,
    Router,
};
use sqlx::{
    sqlite::SqlitePool,
};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> { // use async fn main() -> anyhow::Result<()> for human readable errors
    
    // Connections pool to db
    let pool = SqlitePool::connect("sqlite://anki.db?mode=rwc").await?; // pool is pointer to the pool
    sqlx::migrate!().run(&pool).await?; // question mark returns error if there is a failure

    // with_state takes ownership and would make pool not usable by following code, this is why we clone it
    let app = Router::new()
        .route("/health", get(health))
        .route("/users/count", get(count_users))
        .with_state(pool.clone());
    
    let listener = tokio::net::TcpListener::bind("0.0.0.0:3500").await.unwrap();


    axum::serve(listener, app).await.unwrap();

    Ok(())

}

async fn health() -> &'static str {
    "Healthy"
}

async fn count_users(State(pool): State<SqlitePool>) -> Result<Json<u16>, (StatusCode, String)> {
    let query = sqlx::query_scalar("SELECT COUNT(*) FROM users");
    let outcome: u16 = query.fetch_one(&pool).await.map_err(|e| (StatusCode::INTERNAL_SERVER_ERROR, e.to_string()))?; // ? is returning the Err(...) as it sees it (Err((StatusCode::INTERNAL_SERVER_ERROR, e.to_string())))
    Ok(Json(outcome))
}