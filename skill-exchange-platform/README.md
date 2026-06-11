# Skill Exchange & Peer Learning Platform

Production-grade Spring Boot backend where users teach skills, learn from peers, receive AI-assisted partner matches, schedule sessions, chat in real time, and rate each other.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Redis](https://img.shields.io/badge/Redis-7-red)
![Docker](https://img.shields.io/badge/Docker-ready-blue)

## Features

- AI-powered peer matching with mutual-swap, proficiency, availability, location, and rating signals
- JWT authentication with access and refresh tokens
- Skill profiles for teaching and learning goals
- Availability management and conflict-aware session booking
- WebSocket/STOMP real-time chat with Redis-backed online presence
- Ratings, feedback, and automatic badge awards
- Notifications with unread-count caching and WebSocket push
- Email reminders for booked sessions
- Flyway-managed PostgreSQL schema
- Swagger/OpenAPI documentation
- Docker Compose for PostgreSQL, Redis, and the API
- GitHub Actions CI pipeline

## Architecture

The backend follows a layered architecture:

- Controllers handle HTTP/WebSocket transport, validation, and response wrapping.
- Services contain business rules for matching, sessions, ratings, notifications, and badges.
- Repositories isolate database access through Spring Data JPA.
- Redis is used for online presence, match suggestion caching, and unread notification count caching.
- WebClient integrates with OpenAI for optional match-reason enrichment and gracefully falls back to algorithmic results.
- Flyway owns database evolution and keeps Hibernate in `validate` mode for production safety.

## Prerequisites

- Java 17
- Maven 3.9+
- Docker and Docker Compose
- PostgreSQL 16 and Redis 7, or the included Compose stack

## Quick Start

```bash
git clone https://github.com/your-org/skill-exchange-platform.git
cd skill-exchange-platform
cp .env.example .env
```

Fill in `JWT_SECRET`, `OPENAI_API_KEY`, and `SENDGRID_API_KEY` in `.env`.

```bash
docker-compose up postgres redis -d
./mvnw spring-boot:run
```

If you do not use the Maven wrapper in your clone, run `mvn spring-boot:run` instead.

Swagger UI: http://localhost:8080/swagger-ui.html

## API Endpoints

### Authentication

- `POST /api/v1/auth/register` - register a user
- `POST /api/v1/auth/login` - login and receive JWT tokens
- `POST /api/v1/auth/refresh` - refresh access token

### Users

- `GET /api/v1/users/me` - current user profile
- `PUT /api/v1/users/me` - update profile
- `GET /api/v1/users/{id}` - public profile
- `GET /api/v1/users/search?skill=X&type=TEACH&location=Y` - search users
- `GET /api/v1/users/me/skills` - my skills
- `POST /api/v1/users/me/skills` - add skill
- `DELETE /api/v1/users/me/skills/{skillId}` - remove skill

### Skills

- `GET /api/v1/skills` - paginated skills
- `GET /api/v1/skills/categories` - categories
- `POST /api/v1/skills` - create skill as admin
- `GET /api/v1/skills/search?q=Python` - search skills

### Availability

- `GET /api/v1/availability/me` - my slots
- `POST /api/v1/availability` - add slot
- `DELETE /api/v1/availability/{id}` - remove slot

### Matching

- `GET /api/v1/matches/suggestions` - algorithmic and AI-enhanced suggestions
- `POST /api/v1/matches/request` - send request
- `GET /api/v1/matches/received` - incoming requests
- `GET /api/v1/matches/sent` - outgoing requests
- `PUT /api/v1/matches/{id}/accept` - accept request
- `PUT /api/v1/matches/{id}/reject` - reject request

### Sessions

- `POST /api/v1/sessions` - book session
- `GET /api/v1/sessions/upcoming` - upcoming sessions
- `GET /api/v1/sessions/history` - past sessions
- `GET /api/v1/sessions/{id}` - session detail
- `PUT /api/v1/sessions/{id}/cancel` - cancel session
- `PUT /api/v1/sessions/{id}/complete` - complete session

### Chat

- `GET /api/v1/chat/conversations` - conversations
- `GET /api/v1/chat/messages/{userId}` - message history
- `WS /ws` - STOMP endpoint
- `SEND /app/chat.send` - send message
- `SUBSCRIBE /user/queue/messages` - receive messages
- `SUBSCRIBE /user/queue/online` - presence updates

### Ratings

- `POST /api/v1/ratings` - submit rating
- `GET /api/v1/ratings/user/{userId}` - user ratings
- `GET /api/v1/users/me/badges` - my badges

### Notifications

- `GET /api/v1/notifications` - notifications
- `PUT /api/v1/notifications/{id}/read` - mark read
- `PUT /api/v1/notifications/read-all` - mark all read
- `GET /api/v1/notifications/unread-count` - unread count

## Environment Variables

| Variable | Description | Required |
| --- | --- | --- |
| `JWT_SECRET` | HMAC secret with at least 32 characters | Yes |
| `OPENAI_API_KEY` | OpenAI API key for match reasoning | Yes |
| `SENDGRID_API_KEY` | SendGrid API key for email | Yes |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | No |

## Running Tests

```bash
./mvnw test
```

## Database Migrations

Flyway runs migrations from `src/main/resources/db/migration` on application startup. Hibernate uses `ddl-auto: validate`, so schema drift fails fast instead of silently changing production tables.

## Docker

```bash
docker-compose up --build
```

The API starts on `http://localhost:8080`, PostgreSQL on `5432`, and Redis on `6379`.
