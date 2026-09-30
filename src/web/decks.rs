use askama::Template;
use axum::{
    extract::{Form, Path, State}, http::StatusCode, response::{Html, Redirect},
};
use sqlx::{
    sqlite::SqlitePool,
};

use crate::core::{AddExistingDeckFailureReason::{DatabaseError, EmptyFormattedName, ExistingDeck}, DeleteExistingDeckFailureReason, UpdateExistingDeckFailureReason};

use super::auth::AuthUser;

#[derive(serde::Deserialize)]
pub struct DeckInfo {
    deck_name: String,
}

#[derive(Template)] // given the code in the template, at compile time generate the Rust code that will build the page at runtime
#[template(path = "decks.html")]
struct DecksTemplate<'a> { // 'a indicates that the struct must not live longer than the owner of the value of the fields with marked with 'a 
    decks: &'a [crate::core::Deck], 
}

pub async fn create_deck(State(pool): State<SqlitePool>, auth: AuthUser, deck_info: Form<DeckInfo>) -> Result<Redirect, (StatusCode, String)>{
    crate::core::create_deck(&pool, auth.user_id, &deck_info.deck_name)
        .await
        .map_err(|e| {
                match e {
                    DatabaseError(db_error) => (StatusCode::INTERNAL_SERVER_ERROR, db_error.to_string()),
                    ExistingDeck => (StatusCode::CONFLICT, format!("Deck with name {} already exists", deck_info.deck_name)),
                    EmptyFormattedName => (StatusCode::BAD_REQUEST, "Provided name results in only spaces".to_string())
                }
            }
        )?;
    Ok(Redirect::to("/decks")) //now we redirect to keep it simple. we will have to introduce htmx to only refersh the list of decks, instead of refreshing the whole page
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

pub async fn update_deck_name(State(pool): State<SqlitePool>, Path(deck_id): Path<i64>, auth: AuthUser, deck_info: Form<DeckInfo>) -> Result<Redirect, (StatusCode, String)> {
        crate::core::update_deck_name(&pool, auth.user_id, deck_id, &deck_info.deck_name)
        .await
        .map_err(|e| {
                match e {
                    UpdateExistingDeckFailureReason::DatabaseError(db_error) => (StatusCode::INTERNAL_SERVER_ERROR, db_error.to_string()),
                    UpdateExistingDeckFailureReason::UserHasNoSuchDeck => (StatusCode::NOT_FOUND, format!("Deck with name {} not found", deck_info.deck_name)),
                    UpdateExistingDeckFailureReason::ExistingDeckName => (StatusCode::CONFLICT, format!("Deck with name {} already exists", deck_info.deck_name)),
                    UpdateExistingDeckFailureReason::EmptyFormattedName => (StatusCode::BAD_REQUEST, "Provided name results in only spaces".to_string())
                }
            }
        )?;
    Ok(Redirect::to("/decks")) 
}

pub async fn delete_deck(State(pool): State<SqlitePool>, Path(deck_id): Path<i64>, auth: AuthUser) -> Result<Redirect, (StatusCode, String)> {
        crate::core::delete_deck(&pool, auth.user_id, deck_id)
        .await
        .map_err(|e| {
                match e {
                    DeleteExistingDeckFailureReason::DatabaseError(db_error) => (StatusCode::INTERNAL_SERVER_ERROR, db_error.to_string()),
                    DeleteExistingDeckFailureReason::UserHasNoSuchDeck => (StatusCode::NOT_FOUND, "Deck not found".to_string())
                }
            }
        )?;
    Ok(Redirect::to("/decks")) 
}