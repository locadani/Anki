# Anki Web (working title)

A self-hosted, multi-user spaced repetition app written in Rust. You can add cards from anywhere with an internet connection, not only from a desktop.

> **Status:** early development. Login and deck management work. Cards, reviews and the card-creation API are next.

## Why I'm building this

I use Anki every day to learn vocabulary. To add cards from other tools, I first built a Java CLI on top of [AnkiConnect](https://foosoft.net/projects/anki-connect/), and it showed me where the current setup falls short:

- **It only works locally.** AnkiConnect is an add-on that runs inside the desktop app, so cards can only be created while Anki is open on that machine. I want an HTTP API I can reach from anywhere.
- **It's single-user.** The data belongs to one desktop profile.
- **Words need context.** A word is much easier to remember when you see it used in a sentence. I want cards that pair a word with example sentences and custom styling, eventually generated with AI, and Anki makes that hard to automate.

So I'm rebuilding it as a web app I control. The Java version is kept in [`java_legacy/`](java_legacy/) for reference.

## Why Rust

This project is also how I'm learning Rust. I find the language fascinating: the ownership model and type system make whole classes of bugs impossible, and the compiler pushes you to think clearly about who owns data and what can go wrong. I wanted to learn it by building something real I'd use every day, not by working through toy exercises.

It's also a good fit technically: Rust has a mature spaced repetition ecosystem, including [`fsrs-rs`](https://github.com/open-spaced-repetition/fsrs-rs), the Rust implementation of FSRS, the scheduling algorithm Anki itself uses today.

### How I'm using AI on this project

The code is written by hand, with no AI-generated code. That's on purpose: the goal is to actually learn Rust, and fighting the borrow checker myself is part of how it sticks.

I do use AI as a tutor. It points me to the relevant chapter of [The Rust Book](https://doc.rust-lang.org/book/) or the standard library docs when I'm stuck, explains compiler errors, and reviews code I've already written. The comments throughout the source are my own notes on what I learned, left in as a record of the learning process.

The one exception is this README, which was drafted with AI.

## Features

| | Feature |
|---|---|
| ✅ | Username/password login (argon2 hashing, cookie sessions) |
| ✅ | Create, rename and delete decks, isolated per user |
| ✅ | Deck names are normalized (trimmed, lowercased) and must be unique per user |
| 🚧 | Cards inside decks |
| 🚧 | Review sessions scheduled with FSRS |
| 🚧 | HTTP API for creating cards from other tools |
| 🚧 | AI-generated example sentences for new words |

## Architecture

```
src/
├── main.rs      # Router setup: maps URLs to handlers
├── core/        # Domain logic and database access. Knows nothing about HTTP.
└── web/         # Axum handlers: parse requests, call core, map results to responses
templates/       # Askama HTML templates, checked at compile time
migrations/      # SQL migrations, applied automatically on startup
```

The main design rule: **`core` never depends on Axum.** It exposes plain async functions that return typed errors, for example:

```rust
pub enum UpdateExistingDeckFailureReason {
    DatabaseError(sqlx::Error),
    UserHasNoSuchDeck,
    ExistingDeckName,
    EmptyFormattedName,
}
```

`web` decides how each variant becomes an HTTP response (`404`, `409`, `400`, ...). Because Rust requires every `match` to handle all variants, adding a new failure case anywhere in `core` causes a compile error until the web layer handles it.

Every deck query is filtered by the logged-in user's id, so one user can never read or change another user's data.

## Tech stack

- [Axum](https://github.com/tokio-rs/axum): web framework
- [sqlx](https://github.com/launchbadge/sqlx) with SQLite: database access and migrations
- [Askama](https://github.com/askama-rs/askama): type-safe HTML templates
- [tower-sessions](https://github.com/maxcountryman/tower-sessions): session management
- [argon2](https://docs.rs/argon2): password hashing

## Running locally

Requires a recent stable Rust toolchain (edition 2024).

```sh
cargo run
```

On startup, the app creates `anki.db` in the project directory, applies the migrations, and listens on <http://localhost:3500>.

There is no sign-up page yet, so users have to be inserted into the `users` table directly, with an argon2 hash in `password_hash`.

## Known limitations

- Sessions are stored in memory, so everyone is logged out when the server restarts.
- The UI is plain server-rendered HTML; every action reloads the page.
- No automated tests yet.
