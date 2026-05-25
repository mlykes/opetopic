# Opetopic

Scala/Play Framework 2.6 + Scala.js web app for interactive opetopic diagram editing.
Deployed at **masonlykes.com/opetopic**.

## Dev environment

You are likely running inside the devcontainer (JDK 11 + SBT + Node.js).
A local PostgreSQL instance (`dev-db`) is available at `localhost:5432`.

To start the dev server:
```bash
sbt run
```
App is available at `localhost:9000` (no `/opetopic` prefix in dev — that's only added in production).

## Deploying

Push to the `prod` branch → GitHub Actions SSHes into the server and runs:
```
git pull origin prod && docker-compose up -d --build opetopic
```

Production runs at `masonlykes.com/opetopic` with `play.http.context=/opetopic`.

## Project structure

- `opetopic-play/` — Play Framework backend (controllers, views, routes, Slick/PostgreSQL DAOs)
- `opetopic-core/` — Cross-platform Scala library (compiles to JVM and JS)
- `opetopic-js/` — Scala.js frontend library
- `opetopic-studio/`, `opetopic-lf/`, `opetopic-multiedit/`, `opetopic-docs/` — Scala.js apps

## Notes

- Scala 2.11.12, SBT 0.13.17, Play 2.6.15, Scala.js 0.6.23 — old stack, don't upgrade casually
- Production binary is named `opetopicplay` (all lowercase, no hyphens)
- Auth (Silhouette/OAuth) is present in the code but not yet configured for production
- Play evolutions auto-apply on startup in production
