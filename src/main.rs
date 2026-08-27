mod core;
mod web;
use axum::{
    routing::get,
    Router,
};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> { // use async fn main() -> anyhow::Result<()> for human readable errors
    
    let pool = core::connect().await?;

    // with_state takes ownership and would make pool not usable by following code, this is why we clone it
    let app = Router::new()
        .route("/health", get(health))
        .route("/users/count", get(web::count_users))
        .with_state(pool.clone());
    
    let listener = tokio::net::TcpListener::bind("0.0.0.0:3500").await.unwrap();


    axum::serve(listener, app).await.unwrap();

    Ok(())

}

async fn health() -> &'static str {
    "Healthy"
}
