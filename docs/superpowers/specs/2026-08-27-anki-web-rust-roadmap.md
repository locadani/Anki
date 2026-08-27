# Learning roadmap: self-hosted spaced-repetition web app in Rust

Companion to `2026-08-27-anki-web-rust-design.md`. Read that first for the
architecture and decisions; this document is the ordered sequence of
milestones for building it.

**How to use this**: work through milestones in order. Each one is sized to
introduce a small number of new Rust concepts on top of the previous
milestone, rather than requiring several unfamiliar things at once. You
write all the code — Claude's role at each milestone is explaining the new
concepts before you start, answering questions, and reviewing what you
write. "Done when" is a rough completion signal, not a rigid spec — pace
and exact shape should adapt as you go.

---

## M0 — Bare Axum server

**Goal**: a Rust binary that starts an HTTP server and responds to a
request.

**New concepts**: `Cargo.toml`/crates, `async fn main`, the Tokio runtime,
a minimal Axum router, running/curling a local server.

**Done when**: `cargo run` starts a server; `curl localhost:PORT/health`
returns something.

## M1 — SQLite wiring

**Goal**: the app connects to a SQLite database and runs its first
migration.

**New concepts**: `sqlx`, connection pools, `async`/`.await` around I/O,
`Result`/`?` for fallible operations, SQL migrations, a `users` table
existing (empty for now).

**Done when**: an endpoint that queries the DB (even something trivial like
a row count) works end-to-end.

## M2 — Auth

**Goal**: login/logout with sessions; at least one protected route.

**New concepts**: password hashing (`argon2`), session middleware
(`tower-sessions` or similar), Axum extractors, request middleware/guards,
custom error types.

**Done when**: you can log in as a seeded user, hit a protected route, get
rejected when logged out.

## M3 — Decks

**Goal**: per-user deck list — create a deck, list your own decks.

**New concepts**: shared application state, Askama templates (or your
chosen templating approach), mapping DB rows to structs, ownership/borrowing
questions that come up once you're passing data between DB → handler →
template.

**Done when**: logged-in user can create a deck and see it in their list;
another user doesn't see it.

## M4 — Cards (manual add only)

**Goal**: cards live inside decks; manual add-card form; list cards in a
deck.

**New concepts**: richer structs (card state as an enum), form
deserialization (`serde`), foreign-key relationships in queries.

**Done when**: can add a card to a deck manually and see it listed.

## M5 — FSRS integration

**Goal**: wrap `fsrs-rs` in `core` with a small, tested interface (e.g.
"given a card's current state and a rating, return its next state").

**New concepts**: consuming an external crate's API/types, unit testing in
Rust, keeping `core` free of any web-layer dependency (the architecture
boundary from the design doc).

**Done when**: unit tests cover a few rating sequences and produce sane
scheduling output — no web UI involved yet.

## M6 — Review loop

**Goal**: the actual study screen — fetch next due card, submit a rating,
get rescheduled, move to the next card.

**New concepts**: htmx partial-page updates (server returns an HTML
fragment instead of a full page), tying `core`'s FSRS wrapper into a real
request/response cycle, handling "no cards due" gracefully.

**Done when**: you can review a deck end-to-end and see due dates change
over repeated reviews.

## M7 — AI-assisted card creation

**Goal**: port the existing ChatGPT-based word → card generation flow from
the Java project, wired in as a second path into card creation.

**Before this milestone starts**: a short design session on the actual UX
(how the user triggers it, what they see/can edit before saving) — this was
deliberately left undesigned in the main spec.

**New concepts**: async HTTP calls (`reqwest`), JSON (de)serialization
against an external API, error handling across a network boundary (the API
can fail, rate-limit, time out).

**Done when**: can generate a card from a word and save it, with the same
review-ability as a manually added card.

---

## Deferred (post-MVP, not scheduled yet)

- `.apkg` import.
- Stats/tags/search.
- Off-host backup (Litestream or similar).
- Revisiting the frontend (SPA/WASM) if desired later.
