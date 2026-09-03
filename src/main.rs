mod core;
mod web;
use axum::{
    routing::{get, post},
    Router,
};
use tower_sessions::{MemoryStore, SessionManagerLayer};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> { // use async fn main() -> anyhow::Result<()> for human readable errors
    
    let pool = core::connect().await?;

    let session_store = MemoryStore::default();
    let session_layer = SessionManagerLayer::new(session_store); // If handler succeeds, then add or update session data (cookies)

    // with_state takes ownership and would make pool not usable by following code, this is why we clone it
    let app = Router::new()
        .route("/health", get(health))
        .route("/users/count", get(web::count_users))
        .route("/users/me", get(web::me))
        .route("/login", post(web::login))
        .route("/logout", post(web::logout))
        .layer(session_layer) // runs before and after the handler handles the routes
        .with_state(pool.clone()); // database connection
    
    let listener = tokio::net::TcpListener::bind("0.0.0.0:3500").await.unwrap();
    axum::serve(listener, app).await.unwrap();

    Ok(()) // Never reached because of axum::serve
}

async fn health() -> &'static str {
    "Healthy"
}
