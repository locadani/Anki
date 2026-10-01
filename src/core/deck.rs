use sqlx::sqlite::SqlitePool;

use crate::core::deck_utils::format_deck_name;

#[derive(serde::Serialize)]
#[derive(sqlx::FromRow)]
pub struct Deck {
    pub id: i64,
    user_id: i64,
    pub name: String
}

pub enum AddExistingDeckFailureReason {
    DatabaseError(sqlx::Error),
    ExistingDeck,
    EmptyFormattedName
}

pub enum UpdateExistingDeckFailureReason {
    DatabaseError(sqlx::Error),
    UserHasNoSuchDeck,
    ExistingDeckName,
    EmptyFormattedName
}


pub enum DeleteExistingDeckFailureReason {
    DatabaseError(sqlx::Error),
    UserHasNoSuchDeck,
}


pub async fn create_deck(pool: &SqlitePool, user_id: i64, deck_name: &str) -> Result<(), AddExistingDeckFailureReason> {
    let formatted_deck_name = format_deck_name(deck_name);
    if formatted_deck_name.is_empty() {
        return Err(AddExistingDeckFailureReason::EmptyFormattedName);
    }
    let query = sqlx::query("INSERT INTO decks (user_id, name) VALUES (?,?)")
        .bind(user_id)
        .bind(formatted_deck_name);
    let result = query.execute(pool).await;
    match result {
        Ok(_) => Ok(()),
        Err(e) => {
            match e.as_database_error() { // with e.as_database_error() we check if the error is a db error. it returns a Option(reference) to the possible database error
                Some(database_error) => {
                    if database_error.is_unique_violation() {
                        Err(AddExistingDeckFailureReason::ExistingDeck)
                    }
                    else {
                        Err(AddExistingDeckFailureReason::DatabaseError(e))
                    }
                },
                None => Err(AddExistingDeckFailureReason::DatabaseError(e))
                // in both DatabaseError(e), the ownership of e goes to the AddExistingDeckFailureReason enum (because there is no &e when passing e), which then gets passed to the caller of create_deck
                // LIFETIME
                // we could not return database_error because its lifetime is tied to e. When the function is over, e is gone and also database_error would be invalid since it is a reference
            }
        }
    }
}

pub async fn list_decks(pool: &SqlitePool, user_id: i64) -> Result<Vec<Deck>, sqlx::Error> {
    let query = sqlx::query_as::<_, Deck>("SELECT * FROM decks WHERE user_id = ?")
        .bind(user_id);
    let result =   query.fetch_all(pool).await?;
    Ok(result)
}

pub async fn update_deck_name(pool: &SqlitePool, user_id: i64, deck_id: i64, new_deck_name: &str) -> Result<(), UpdateExistingDeckFailureReason> {
    let formatted_deck_name = format_deck_name(new_deck_name);
    if formatted_deck_name.is_empty() {
        return Err(UpdateExistingDeckFailureReason::EmptyFormattedName);
    }
    let query = sqlx::query("UPDATE decks SET name = ? WHERE user_id = ? AND id = ?")
        .bind(formatted_deck_name)
        .bind(user_id)
        .bind(deck_id);
    let result =   query.execute(pool).await;
    match result {
        Ok(query_result) => {
            if query_result.rows_affected() > 0 {
                Ok(())
            }
            else {
                Err(UpdateExistingDeckFailureReason::UserHasNoSuchDeck)
            }
        }
        Err(e) => {
            match e.as_database_error() { // with e.as_database_error() we check if the error is a db error. it returns a Option(reference) to the possible database error
                Some(database_error) => {
                    if database_error.is_unique_violation() {
                        Err(UpdateExistingDeckFailureReason::ExistingDeckName)
                    }
                    else {
                        Err(UpdateExistingDeckFailureReason::DatabaseError(e))
                    }
                },
                None => Err(UpdateExistingDeckFailureReason::DatabaseError(e))
            }
        }
    }
}



pub async fn delete_deck(pool: &SqlitePool, user_id: i64, deck_id: i64) -> Result<(), DeleteExistingDeckFailureReason> {
    let query = sqlx::query("DELETE FROM decks WHERE user_id = ? AND id = ?")
        .bind(user_id)
        .bind(deck_id);
    let result =   query.execute(pool).await;
    match result {
        Ok(query_result) => {
            if query_result.rows_affected() > 0 {
                Ok(())
            }
            else {
                Err(DeleteExistingDeckFailureReason::UserHasNoSuchDeck)
            }
        }
        Err(e) => Err(DeleteExistingDeckFailureReason::DatabaseError(e))
    }
}
