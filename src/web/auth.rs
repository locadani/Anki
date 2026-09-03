use axum::{
    extract::{Form, FromRequestParts, State},
    http::{request::Parts, StatusCode},
    Json,
    response::Redirect,
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


pub struct AuthUser {
    pub user_id: i64,
}

impl<S: Send + Sync> FromRequestParts<S> for AuthUser {
    type Rejection = (StatusCode, String);
    async fn from_request_parts(parts: &mut Parts, state: &S) -> Result<Self, Self::Rejection> { // self means "the type after for " AuthUser int his case
        let session = Session::from_request_parts(parts, state)
            .await
            .map_err(|(status, msg)| (status, msg.to_string()))?;
        let user_id = session.get::<i64>("user_id").await;
        match user_id {
            Ok(Some(id)) => Ok(AuthUser { user_id: id }),
            Ok(None) => Err((StatusCode::UNAUTHORIZED, "not logged in".to_string())),
            Err(e) => Err((StatusCode::INTERNAL_SERVER_ERROR, e.to_string()))
        }
    }
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

// session is the session related to the user calling this function
pub async fn logout(session: Session) -> Result<Redirect, (StatusCode, String)>{
    let result = session.delete().await;
    match result {
        Ok(_) => Ok(Redirect::to("/login")),
        Err(e) => Err((StatusCode::INTERNAL_SERVER_ERROR, e.to_string())),
    }
}

pub async fn me(auth: AuthUser) -> Result<Json<i64>, (StatusCode, String)>{
    let my_id = auth.user_id;
    Ok(Json(my_id))
}