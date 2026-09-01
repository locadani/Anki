// This module must not be aware of anything from Axum, as axum is a router dealing with network, while core is about functionalities
// In this file show only what functions and Types are public
mod user;
mod auth;
mod db;

pub use auth::verify_password;
pub use user::find_by_username;
pub use db::{connect, count_users};