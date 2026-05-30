# Opetopic

Scala/Play Framework 2.6 + Scala.js web app for interactive opetopic diagram editing.
Deployed at **masonlykes.com/opetopic**.

## Dev environment

You are likely running inside the devcontainer (JDK 11 + SBT + Node.js).
A local PostgreSQL instance (`dev-db`) is available at `localhost:5432`.

**Starting the dev server:** Press **F5** in VS Code. This runs the "Play: Debug" launch config, which:
1. Starts `sbt -Dplay.http.context=/opetopic -jvm-debug 5005 opetopicPlay/run` in a background terminal
2. Waits for Play to log "Listening for HTTP on", then attaches the Java debugger on port 5005

App is available at **`localhost:8890/opetopic/`** via the dev nginx (port-forwarded from server:8889).
Play's built-in hot reload is active — save a file and the next request triggers recompile.

To stop: kill the "Play: start dev server" terminal (the debugger detaches automatically).

**How the dev routing works:**
- `localhost:8890` → VS Code port-forward → server:8889 → `mlykes-nginx-dev` container
- nginx proxies `/opetopic` → `opetopic-dev:9000` (the devcontainer's network alias on `opetopic-proxy`)
- The prod container (`opetopic:9000`) is unaffected and continues serving `masonlykes.com/opetopic`

The `-Dplay.http.context=/opetopic` flag is required so Play routes match the `/opetopic` prefix
that nginx forwards. This mirrors how the production container is configured (see `Dockerfile` ENTRYPOINT).

## Deploying

Push to the `prod` branch → GitHub Actions:
1. Builds the Docker image and pushes to `ghcr.io/mlykes/opetopic:latest`
2. SSHes into the server and runs `docker-compose pull opetopic && docker-compose up -d` from `/opt/opetopic-prod/`

Production runs at `masonlykes.com/opetopic` with `play.http.context=/opetopic`.
Production compose and secrets live in `/opt/opetopic-prod/` (not in this repo).

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
