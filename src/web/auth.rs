use axum::{
    extract::{Form, State},
    http::StatusCode,
    response::Redirect
};
use sqlx::{
    sqlite::SqlitePool,
};
use tower_sessions::Session;

#[derive(serde::Deserialize)]
pub struct Login {
    username: String,
    password: String,
}

// session is created by the session manager if not existed, or just passed if already existing
// at the end of this function, the session manager notices that the session changed and will handle it
pub async fn login(State(pool): State<SqlitePool>, session: Session, login_form: Form<Login>) -> Result<Redirect, (StatusCode, String)>{
    let user = crate::core::find_by_username(&pool, &login_form.username)
        .await
        .map_err(|e| (StatusCode::INTERNAL_SERVER_ERROR, e.to_string()))?;
    match user {
        Some(user) => {
            if crate::core::verify_password(&login_form.password, &user.password_hash) {
                session.insert("user_id", user.id).await.map_err(|e| (StatusCode::INTERNAL_SERVER_ERROR, e.to_string()))?;
                Ok(Redirect::to("/")) // Homepage
            } else {
                Err((StatusCode::UNAUTHORIZED, "invalid credentials".to_string()))
            }
        }
        None => Err((StatusCode::UNAUTHORIZED, "invalid credentials".to_string())),
    }
}