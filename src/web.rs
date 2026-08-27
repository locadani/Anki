use axum::{
    extract::State,
    http::StatusCode,
    Json,
};

use sqlx::{
    sqlite::SqlitePool,
};

pub async fn count_users(State(pool): State<SqlitePool>) -> Result<Json<u16>, (StatusCode, String)> {
    let outcome: u16 = crate::core::count_users(&pool).await.map_err(|e| (StatusCode::INTERNAL_SERVER_ERROR, e.to_string()))?; // ? is returning the Err(...) as it sees it (Err((StatusCode::INTERNAL_SERVER_ERROR, e.to_string())))
    Ok(Json(outcome))
}