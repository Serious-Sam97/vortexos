# VortexOS — Backend

The server side of [VortexOS](../vortexos-frontend). A Spring Boot + SQLite service that
provides the three things the in-browser OS can't do on its own:

1. **Accounts & auth** — register/login, BCrypt-hashed passwords, stateless JWTs.
2. **The cloud drive** — the `File` table behind the OS's `/mnt/cloud` mount, scoped per user.
3. **GameCache** — the per-user game library behind the desktop's *GameCache* (Backlogger) app.

Everything except `/auth/**` requires an `Authorization: Bearer <jwt>` header, and every
`File`/`Game` row is owned by — and only visible to — the user in that token.

## Stack

- **Java 24**, **Spring Boot 3.4** (Gradle).
- **Spring Security** + **JWT** (`io.jsonwebtoken:jjwt` 0.12).
- **Spring Data JPA** over **SQLite** (`org.xerial:sqlite-jdbc` + `hibernate-community-dialects`).

## Run

```bash
./gradlew bootRun        # serves on http://localhost:8082
```

The schema is created/updated automatically (`ddl-auto=update`) in `vortexos.db`. Delete
that file to start from a clean database.

```bash
./gradlew test           # JUnit + MockMvc + @DataJpaTest (36 tests)
```

## API surface

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/auth/register` | — | `201 {token, username}` · `400` blank · `409` duplicate |
| POST | `/auth/login` | — | `200 {token, username}` · `401` bad credentials |
| GET/POST/PUT/DELETE | `/games`, `/games/backlog`, `/games/{id}…` | ✅ | per-user game library |
| GET | `/platforms` | ✅ | shared reference data (seeded) |
| GET/POST/DELETE/PUT | `/files`, `/files/rename` | ✅ | per-user cloud drive (paths like `/mnt/cloud/…`) |

Unauthenticated requests to a protected endpoint get **401** (not the Spring-default 403).

## Layout

```
src/main/java/com/serioussam/vortexos/
  domain/                 entities + repository interfaces
    user/  game/  platform/  file/
  infrastructure/
    repository/           Spring Data Jpa* repositories
    seeder/               PlatformSeeder (idempotent)
  application/
    controller/           AuthController, GameController, PlatformController, FileController
    dto/                  request/response records & DTOs
    security/             JwtService, JwtAuthFilter, CustomUserDetailsService,
                          SecurityConfig, CurrentUser
src/main/resources/application.properties   # port 8082, jwt.secret / jwt.expiration
src/test/...                                # mirrors the above
```

## Configuration

- `server.port=8082`
- `jwt.secret` / `jwt.expiration` — HMAC secret (base64, ≥256-bit) and token lifetime (ms).
  Override the secret via the `JWT_SECRET` env var in any real deployment.
- CORS is configured in `SecurityConfig` (allows the dev frontend `:5173` and the Vercel deploy).

## How it fits the OS

The auth model, JWT pieces, per-user ownership, and how the frontend attaches the token are
documented in **[../vortexos-frontend/docs/MULTIUSER.md](../vortexos-frontend/docs/MULTIUSER.md)**.
