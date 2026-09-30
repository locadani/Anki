mod auth;
mod decks;

use axum::{
    extract::State,
    http::StatusCode,
    Json,
};

use sqlx::{
    sqlite::SqlitePool,
};

// pub use ... allows the other mods to use these values
pub use auth::{login, login_page, logout, me, verify_user_and_redirect};
pub use decks::{create_deck, delete_deck, list_decks, update_deck_name};

pub async fn count_users(State(pool): State<SqlitePool>) -> Result<Json<u16>, (StatusCode, String)> {
    let outcome: u16 = crate::core::count_users(&pool).await.map_err(|e| (StatusCode::INTERNAL_SERVER_ERROR, e.to_string()))?; // ? is returning the Err(...) as it sees it (Err((StatusCode::INTERNAL_SERVER_ERROR, e.to_string())))
    Ok(Json(outcome))
}
