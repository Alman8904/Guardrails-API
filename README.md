# Guardrails API

A Spring Boot microservice that acts as a central API gateway with Redis-based guardrails for controlling bot interactions.

## Stack
- Java 21, Spring Boot 3.5.13
- PostgreSQL (data storage)
- Redis (guardrails, scoring, notifications)
- Docker

## Running Locally
1. Clone the repo
2. Copy `src/main/resources/application.properties.example` to `application.properties`
3. `docker compose up -d` — starts PostgreSQL and Redis
4. `./mvnw spring-boot:run` — starts the app on port 8080

Two users and two bots are seeded automatically on first run.

## Endpoints
| Method | Path | Body / Params |
|--------|------|---------------|
| POST | `/api/posts` | `content`, `authorType`, `authorId` |
| POST | `/api/posts/{postId}/comments` | `content`, `authorType`, `authorId`, `depthLevel` |
| POST | `/api/posts/{postId}/like` | `userId`, `userType` as query params |

## Virality Scoring
Scores are stored in Redis and updated instantly on every interaction.
- Bot reply → +1
- Human like → +20
- Human comment → +50

## Bot Guardrails
Three checks run before a bot comment is saved to the database:
- **Horizontal cap** — max 100 bot replies per post. Counter stored in Redis as `post:{id}:bot_count`, rejected with 429 beyond that.
- **Vertical cap** — max depth of 20 levels. Rejected if `depthLevel > 20`.
- **Cooldown cap** — a bot cannot interact with the same human more than once per 10 minutes. Stored in Redis as `cooldown:bot_{id}:human_{id}` with a 10 minute TTL. If the key exists, the request is rejected with 429.

## Thread Safety
The horizontal cap uses Redis `INCR` which is atomic. The counter is incremented before checking — if the result exceeds 100 it is immediately decremented and the request is rejected with 429. Since `INCR` is a single atomic operation, no two concurrent requests can read the same value. This guarantees the cap holds at exactly 100 even under 200 simultaneous requests.

The cooldown cap uses Redis keys with TTL set in a single atomic operation, so no race condition is possible there either.

## Notification Engine
When a bot interacts with a post, the system checks if the post owner received a notification in the last 15 minutes using a Redis TTL key `notif_cooldown:user_{id}`.

- If no cooldown exists: logs `Push Notification Sent to User {id}` and sets a 15 minute cooldown.
- If cooldown exists: pushes the message to a Redis list `user:{id}:pending_notifs`.

A scheduler runs every 5 minutes and flushes all pending notifications per user as a single summarized log message, then clears the list.

## Statelessness
All counters, cooldowns, and pending notifications are stored exclusively in Redis. No in-memory state is used anywhere in the application.
