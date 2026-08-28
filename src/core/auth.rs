use argon2::Argon2;
use argon2::password_hash::{PasswordHash, PasswordVerifier};

pub fn verify_password(password: &str, hash: &str) -> bool {
    let parsed_hash: PasswordHash = match PasswordHash::new(hash) {
        Ok(n) => n,
        Err(_) => return false,
    };
    Argon2::default().verify_password(password.as_bytes(), &parsed_hash).is_ok()
}