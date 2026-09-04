use axum::{
    extract::{Form, State},
    http::StatusCode,
    Json,
};
use sqlx::{
    sqlite::SqlitePool,
};

use super::auth::AuthUser;

#[derive(serde::Deserialize)]
pub struct DeckInfo {
    deck_name: String,
}

pub async fn create_deck(State(pool): State<SqlitePool>, auth: AuthUser, deck_info: Form<DeckInfo>) -> Result<Json<bool>, (StatusCode, String)>{
    let result = crate::core::create_deck(&pool, auth.user_id, &deck_info.deck_name)
        .await
        .map_err(|e| (StatusCode::INTERNAL_SERVER_ERROR, e.to_string()))?;
    Ok(Json(result))
}