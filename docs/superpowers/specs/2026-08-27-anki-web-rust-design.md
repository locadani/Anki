# Design: self-hosted multi-user spaced-repetition web app (Rust rewrite)

## Purpose

Replace the current setup — a Java/Spring Boot CLI tool that requires Anki
Desktop running locally and talks to it via AnkiConnect — with a self-hosted,
multi-user web app that owns its own scheduling and storage. Two goals, both
load-bearing:

1. **Practical**: review flashcards from a browser, for 2-10 known users
   (family/friends), without needing Anki Desktop open on a PC.
2. **Learning**: this is also a project to learn Rust. The implementation
   choices below are optimized to maximize genuine Rust learning surface
   without adding unrelated complexity (a second frontend stack, a
   speculative multi-tenant SaaS architecture, etc.).

## Non-goals (v1)

- Staying compatible with Anki's file format, sync protocol, or apps
  (AnkiDroid/AnkiMobile/Anki Desktop). This is a clean break — own data
  model, own web UI, no `.apkg`/sync involved.
- `.apkg` import. Considered and deferred — reintroduces Anki's SM-2
  scheduling-data-to-FSRS mapping problem, which isn't a clean translation.
  Revisit post-MVP if wanted.
- Note-type templates, media (images/audio), tags, filtered decks, stats
  dashboards, deck sharing, add-ons. All standard Anki features, none in
  scope for v1.
- Real multi-tenant/SaaS concerns (public signup, billing, horizontal
  scaling, tenant isolation beyond per-user rows). Scale target is 2-10
  people the owner knows, self-hosted on infrastructure the owner controls.

## v1 scope

- Login (2-10 users, accounts created directly by the owner — no self-serve
  signup, no email verification).
- Per-user decks.
- Cards per deck, each carrying FSRS scheduling state.
- Add card: manual entry, and AI-assisted generation (the ChatGPT-based
  word → translation + example flow already built in the existing Java
  project). UX details for the AI-assisted flow are intentionally left
  undesigned here — to be brainstormed in a dedicated session before that
  milestone is implemented.
- Review/study screen: shows the next due card, accepts a rating
  (Again/Hard/Good/Easy), reschedules via FSRS.

## Why these technology choices

Each choice below was evaluated against three constraints the user gave
explicitly: (a) maximize genuine Rust-learning value, (b) minimize RAM
footprint and hosting cost (target: $0/month), (c) keep everything else
"simple" so effort concentrates on Rust itself.

### Scheduling algorithm: FSRS via `fsrs-rs`

FSRS (Free Spaced Repetition Scheduler) is the algorithm modern Anki itself
uses. Its **reference implementation is written in Rust**
(`open-spaced-repetition/fsrs-rs` — the Python/TypeScript bindings are
themselves generated *from* this), so building on it directly means zero
version-lag risk by construction, at the cost of a slightly lower-level API
than the end-user-friendly `py-fsrs`/`ts-fsrs` wrappers (acceptable — the
glue code is itself part of the learning value here).

### Backend/web framework: Rust + Axum

Considered Java (keep the existing codebase), Python/FastAPI, and
TypeScript/Next.js as alternatives (see prior research in conversation
history). Rejected all three because the explicit goal is learning Rust —
this is not a "best tool for the job in the abstract" decision, it's a
"best tool for the stated goal" decision. Within Rust, Axum was chosen over
Actix-web as the more commonly recommended default for 2026 (built on
Tokio/Tower/Hyper, richer middleware ecosystem, better docs); Actix's extra
throughput headroom is irrelevant at 2-10 users.

### Frontend: server-rendered (Askama templates + htmx), not a SPA

Considered a separate SPA (React) and a full-stack Rust/WASM frontend
(Leptos). Rejected both for v1: a SPA means learning a second stack in
parallel with Rust; Leptos/WASM tooling is the least mature of the options
and would roughly double the learning curve of a first Rust project.
Server-rendered HTML + htmx keeps 100% of the new-to-the-user surface area
in Rust, where the actually interesting problems live (FSRS integration,
async web server, auth, database access).

**Explicitly kept in mind for later**: the codebase is structured (see
Architecture below) so the frontend can be swapped for something more
capable later without touching the core domain logic.

### Storage: SQLite via `sqlx`, not Postgres

Chosen for zero operational overhead (no second process to run, configure,
or pay RAM for), trivial backup (it's one file), and because it comfortably
handles this app's actual load (read-heavy, light/infrequent writes, 2-10
users). Confirmed this doesn't box the project in:
- `sqlx` uses near-identical query syntax across SQLite/Postgres, so a
  future migration is a data-migration + config change, not a rewrite.
- Crash safety is equivalent to Postgres — SQLite's WAL/journal gives the
  same ACID guarantees; a process crash mid-transaction is handled
  automatically on next startup, no corruption, no silent data loss for
  committed transactions.
- The one real gap — **whole-host loss** (disk/VM destroyed) — is not a
  SQLite-specific weakness; it applies to any local database without
  off-host backup. Mitigation (continuous off-host backup via Litestream,
  streaming to e.g. Backblaze B2's free tier) was discussed and deliberately
  **deferred**, not rejected — revisit once the app is in real use.
- **Operational note for deployment**: the SQLite file must live on a
  mounted persistent volume, not inside a container's ephemeral filesystem,
  or a redeploy/container recreation will wipe it. This is a container
  hygiene issue, not specific to SQLite (the same rule would apply to a
  self-hosted Postgres data directory).

### Hosting: self-hosted, $0 target

Single Rust binary + one SQLite file. Fits comfortably on Oracle Cloud's
Always-Free ARM tier (2 OCPU / 12GB RAM as of mid-2026) or a home
server/Raspberry Pi (4GB+ recommended). Fly.io and Railway were ruled out —
both removed their permanent free tiers as of 2024-2025 research findings.

## Architecture

One Rust binary, internally layered:

- **`core`**: domain logic only — `User`, `Deck`, `Card`, `Review` models;
  FSRS scheduling (wraps `fsrs-rs`); card-creation service (manual +,
  later, AI-assisted); persistence via `sqlx`/SQLite. No knowledge of
  HTTP/HTML.
- **`web`**: Axum routes, session-based auth middleware, Askama templates,
  htmx-driven partial updates. Talks to `core` only through its public
  functions/services.

This boundary is what makes "swap the frontend later" cheap: `core` doesn't
change; a future SPA either gets JSON endpoints added alongside the HTML
ones, or `web` is replaced wholesale.

## Data model (SQLite)

- `users` (id, username, password_hash)
- `decks` (id, user_id, name)
- `cards` (id, deck_id, front, back, fsrs_stability, fsrs_difficulty,
  due_at, reps, lapses, state)
- `reviews` (id, card_id, reviewed_at, rating, elapsed_days,
  scheduled_days) — full review history; not used by any UI in v1, but
  kept from day one since FSRS's optimizer can retrain scheduling
  parameters from this log later, and it's much cheaper to log from the
  start than to backfill.

## Key flows

- **Auth**: username/password (argon2 hash), session cookie. Owner creates
  accounts directly.
- **Review loop**: query the next due card for a deck (`due_at <= now`,
  earliest first) → render front → reveal back → user rates → `fsrs-rs`
  computes the new schedule → persist updated card + new `reviews` row →
  htmx swaps in the next card without a full page reload.
- **Add card**: manual form now; AI-assisted generation as a second path
  into the same card-creation service, UX deferred to a future design
  session before that milestone starts.
- **Deferred**: `.apkg` import (see Non-goals).

## Testing

- Unit tests for `core` logic: FSRS wrapper, card-creation service.
- Integration tests for `web`: Axum routes against a temporary SQLite DB.

## Collaboration model (this is a load-bearing project constraint)

**The user writes all implementation code, including scaffolding and
boilerplate.** This is explicit and non-negotiable for this project: the
point is to learn Rust, not to have a working app that someone else's AI
wrote. Claude's role is limited to:

- Explaining Rust/library concepts *before* the user needs them for a given
  milestone.
- Answering questions when stuck.
- Reviewing and critiquing code the user shares — including flagging
  non-idiomatic patterns — without rewriting it wholesale.
- Helping debug by explaining errors/compiler messages, not by silently
  fixing them.

Because of this, the standard "write an implementation plan, then execute
it" flow doesn't apply here — there's a companion document instead:
`2026-08-27-anki-web-rust-roadmap.md`, a milestone-based learning roadmap
the user works through themselves.
