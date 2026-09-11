# ShopHub Full Stack

ShopHub is a full-stack local deals and reviews platform inspired by apps such as Yelp and Dianping. It demonstrates a React frontend backed by a Spring Boot API with Redis-powered authentication, caching, geo search, social feeds, and high-concurrency flash-sale ordering.

## What It Shows

- Mobile-first React experience for browsing shops, reviews, follows, comments, check-ins, and vouchers
- Token login with Redis-backed session storage
- Cache-aside shop reads with cache-penetration protection
- Redis GEO queries for nearby shops
- Follow relationships and feed timelines
- Flash-sale voucher ordering with Lua, Redis Streams, Redisson locks, and MySQL constraints
- Docker Compose setup for the frontend, backend, MySQL, and Redis

## Architecture

```text
Browser
  |
  | React + Vite build served by Nginx
  v
Frontend container
  |
  | /api/* reverse proxy
  v
Spring Boot API
  |
  +-- MySQL: users, shops, blogs, vouchers, orders
  |
  +-- Redis: login tokens, verification codes, cache, geo indexes,
              likes, social feeds, flash-sale stock, order stream
```

The backend follows a conventional layered structure:

```text
controller -> service -> mapper -> MySQL
                    |
                    +-> Redis
```

## Project Structure

```text
ShopHub-fullstack
+-- ShopHub-frontend/     React + Vite + TypeScript app
+-- ShopHub-backend/      Spring Boot backend API
+-- docker-compose.yml    Full local runtime
+-- start.sh              Local Docker startup helper
`-- stop.sh               Local Docker shutdown helper
```

## Tech Stack

### Frontend

- React
- TypeScript
- Vite
- React Router
- Axios
- Lucide React
- Nginx container for production serving and API proxying

### Backend

- Java 8
- Spring Boot 2.3
- MyBatis-Plus
- MySQL 5.7
- Redis
- Redisson
- Redis Lua scripts
- Redis Streams
- JUnit 5 and Mockito

## Main User Flows

### Authentication

Users request a verification code with a phone number. The code is stored in Redis with a short TTL. In local demo mode, the code is written to the backend log instead of being sent through a real SMS provider. After login, the frontend stores the returned token and sends it through the `authorization` header.

### Shop Discovery

The frontend supports category browsing, keyword search, merchant detail pages, and a sample nearby-shop view that calls the backend Redis GEO query path.

### Vouchers and Flash Sales

Shop detail pages show voucher data from the backend. Flash-sale voucher claims call the Redis Lua admission-control flow, enqueue accepted requests into Redis Streams, and persist orders asynchronously in MySQL.

### Social Reviews

Users can view hot posts, open post details, like posts, inspect recent likes, comment, publish new review posts, and follow other users.

### Profile

The profile page shows the signed-in user, daily check-in status, check-in streak, and the user's own review posts.

## Running Locally

### Option 1: Docker Compose

From the repository root:

```bash
docker compose up --build
```

Then open:

```text
Frontend:    http://localhost:8080
Backend API: http://localhost:8081
```

If your environment still uses the old Compose binary:

```bash
docker-compose up --build
```

Optional environment variables:

```bash
MYSQL_ROOT_PASSWORD=your_password
```

### Option 2: Local Development

Start MySQL and Redis, initialize MySQL with:

```text
ShopHub-backend/src/main/resources/db/hmdp.sql
```

Run the backend:

```bash
cd ShopHub-backend
mvn spring-boot:run
```

Recommended backend JDK: 17. The backend still targets Java 8 bytecode for compatibility, so JDK 24 may print `source value 8 is obsolete` warnings in IDE builds. Those warnings are not build failures.

The default local JDBC URL includes `allowPublicKeyRetrieval=true` so the demo can connect to local MySQL 8 instances that use `caching_sha2_password`. Use a stronger SSL-backed configuration for production.

Run the frontend:

```bash
cd ShopHub-frontend
npm install
npm run dev
```

The Vite dev server proxies `/api/*` requests to `http://localhost:8081`.

## Free Cloud Deployment

For a no-cost interview demo, the recommended path is a free-tier VPS running Docker Compose. See:

```text
DEPLOY_FREE.md
```

The deployment compose file is:

```bash
docker compose -f docker-compose.free.yml up -d --build
```

It publishes only the frontend entry point and keeps MySQL and Redis private inside the Docker network.

## Demo Login

Request a login code:

```bash
curl -X POST "http://localhost:8081/user/code?phone=13686869696"
```

Read the verification code from the backend log, then sign in through the frontend or call:

```bash
curl -X POST "http://localhost:8081/user/login" \
  -H "Content-Type: application/json" \
  -d '{"phone":"13686869696","code":"123456"}'
```

Use the returned token for authenticated endpoints:

```bash
curl "http://localhost:8081/user/me" \
  -H "authorization: <token>"
```

## Database

The schema and seed data live in:

```text
ShopHub-backend/src/main/resources/db/hmdp.sql
```

The seed data is intentionally preserved as localized sample content. The frontend adds English labels and summaries around the main demo flows so the project is understandable to international reviewers.

Important constraints and indexes include:

- `tb_user.phone` unique index
- `tb_follow(user_id, follow_user_id)` unique index
- `tb_voucher_order(user_id, voucher_id)` unique index
- query indexes for shop type, vouchers by shop/status, blogs, and comments

## Testing

Run backend tests:

```bash
cd ShopHub-backend
mvn clean test
```

Run frontend checks:

```bash
cd ShopHub-frontend
npm run build
```

Current backend test coverage includes:

- phone number and verification-code validation
- cache key-prefix behavior
- Redis lock owner-token behavior
- flash-sale Lua script contract checks

## Trade-Offs

- Redis is used for fast admission control during flash sales, while MySQL remains the final source of truth.
- Redis Streams provide lightweight async order processing without introducing a separate message broker.
- The frontend is intentionally product-focused rather than a generic admin dashboard.
- The project does not implement real payment processing, production SMS delivery, or production-grade permission roles.

## Roadmap

- Add Testcontainers-based MySQL and Redis integration tests
- Add OpenAPI documentation
- Add GitHub Actions for backend tests and frontend builds
- Add rate limiting for verification-code requests
- Replace localized seed content with a fully English demo dataset

## License

This project is for learning and interview demonstration purposes.
