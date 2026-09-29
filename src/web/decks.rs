use askama::Template;
use axum::{
    Json, extract::{Form, State}, http::StatusCode, response::Html,
};
use sqlx::{
    sqlite::SqlitePool,
};

use super::auth::AuthUser;

#[derive(serde::Deserialize)]
pub struct DeckInfo {
    deck_name: String,
}

#[derive(Template)] // given the code in the template, at compile time generate the Rust code that will build the page at runtime
#[template(path = "decks.html", print = "code")]
struct DecksTemplate<'a> { // 'a indicates that the struct must not live longer than the owner of the value of the fields with marked with 'a 
    decks: &'a [crate::core::Deck], 
}

pub async fn create_deck(State(pool): State<SqlitePool>, auth: AuthUser, deck_info: Form<DeckInfo>) -> Result<Json<bool>, (StatusCode, String)>{
    let result = crate::core::create_deck(&pool, auth.user_id, &deck_info.deck_name)
        .await
        .map_err(|e| (StatusCode::INTERNAL_SERVER_ERROR, e.to_string()))?;
    Ok(Json(result))
}

// library to integrate Askama and axum https://askama.rs/en/stable/frameworks.html
pub async fn list_decks(State(pool): State<SqlitePool>, auth: AuthUser) -> Result<Html<String>, (StatusCode, String)>{
    let result = crate::core::list_decks(&pool, auth.user_id)
        .await
        .map_err(|e| (StatusCode::INTERNAL_SERVER_ERROR, e.to_string()))?;

    let template = DecksTemplate {
        decks: &result,
    };

    // like logout
    let decks_list_page = template.render();
    match decks_list_page {
        Ok(decks_list_page) => Ok(Html(decks_list_page)),
        Err(e) => Err((StatusCode::INTERNAL_SERVER_ERROR, e.to_string())),
    }
}