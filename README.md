# ShopHub Backend

ShopHub is a Spring Boot backend for a local deals and reviews platform. It focuses on Redis-backed authentication, cache-aside shop lookup, geo search, social feeds, and high-concurrency flash-sale voucher ordering.

This repository currently focuses on the backend system. Frontend and Nginx static assets are intentionally kept out of scope for this backend-focused version.

## Why This Project Exists

The project is designed to demonstrate backend system design trade-offs that commonly appear in marketplace, coupon, and local-services products:

- Token-based login with Redis session storage
- Cache-aside reads with cache penetration protection
- Redis GEO queries for nearby shops
- Follow relationships and feed timelines
- Flash-sale voucher ordering with Lua, Redis Streams, distributed locks, and database constraints

## Architecture

```text
Client
  |
  | HTTP / JSON
  v
Spring Boot API
  |
  +-- MySQL: users, shops, blogs, vouchers, orders
  |
  +-- Redis: login tokens, verification codes, cache, geo indexes,
              likes, social feeds, flash-sale stock, order stream
```

The backend uses a conventional layered structure:

```text
controller -> service -> mapper -> MySQL
                    |
                    +-> Redis
```

## Tech Stack

- Java 8
- Spring Boot 2.3
- MyBatis-Plus
- MySQL 5.7
- Redis
- Redisson
- Redis Lua scripts
- Redis Streams
- JUnit 5 and Mockito
- Docker Compose

## Main Modules

### Authentication

Users request a verification code with a phone number. The code is stored in Redis with a short TTL. After login, the API returns a random token and stores a compact user session in Redis. Interceptors refresh token TTLs and protect authenticated routes.

### Shop Querying

Shop details use cache-aside reads. Empty values are cached briefly to reduce cache penetration. Shop category and nearby-shop queries use Redis data structures to reduce repeated database reads.

### Flash-Sale Voucher Ordering

The flash-sale flow is optimized for concurrent requests:

```text
POST /voucher-order/seckill/{voucherId}
  -> execute Redis Lua script
  -> atomically check stock and duplicate purchase
  -> decrement Redis stock
  -> append order request to Redis Stream
  -> async consumer persists the order in MySQL
  -> database unique index prevents duplicate user-voucher orders
```

The database remains the final source of truth. Redis handles fast admission control; MySQL constraints provide the last line of defense.

### Social Feed

Users can follow other users. When a user publishes a blog post, the post id is pushed into each follower's Redis sorted-set inbox. Feed reads use scroll pagination based on timestamp scores.

### Blog Comments

The backend exposes a minimal comment API for creating comments and listing comments for a blog post. Comment counts are maintained on the blog row.

## Database

The schema lives in:

```text
ShopHub-backend/src/main/resources/db/hmdp.sql
```

The seed data is intentionally preserved as localized sample content. The code, configuration, and project documentation describe the backend design in English.

Important constraints and indexes include:

- `tb_user.phone` unique index
- `tb_follow(user_id, follow_user_id)` unique index
- `tb_voucher_order(user_id, voucher_id)` unique index
- query indexes for shop type, vouchers by shop/status, blogs, and comments

## Running Locally

### Option 1: Docker Compose

From the repository root:

```bash
docker compose up --build
```

If your environment still uses the old Compose binary:

```bash
docker-compose up --build
```

Services:

- Backend API: `http://localhost:8081`
- MySQL: `localhost:3306`
- Redis: `localhost:6379`

Optional environment variables:

```bash
MYSQL_ROOT_PASSWORD=your_password
```

### Option 2: Local JVM

Start MySQL and Redis, initialize MySQL with `hmdp.sql`, then run:

```bash
cd ShopHub-backend
mvn spring-boot:run
```

Local configuration can be overridden with:

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/ShopHub?useSSL=false
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
APP_UPLOAD_DIR=./uploads/imgs/
```

## API Examples

Request a login code:

```bash
curl -X POST "http://localhost:8081/user/code?phone=13686869696"
```

In local development, the verification code is written to the backend log instead of being sent through a real SMS provider.

Login:

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

Query shops by type:

```bash
curl "http://localhost:8081/shop/of/type?typeId=1&current=1" \
  -H "authorization: <token>"
```

Create a flash-sale voucher order:

```bash
curl -X POST "http://localhost:8081/voucher-order/seckill/1" \
  -H "authorization: <token>"
```

Follow a user:

```bash
curl -X PUT "http://localhost:8081/follow/2/true" \
  -H "authorization: <token>"
```

Like a blog:

```bash
curl -X PUT "http://localhost:8081/blog/like/4" \
  -H "authorization: <token>"
```

Create a blog comment:

```bash
curl -X POST "http://localhost:8081/blog-comments" \
  -H "authorization: <token>" \
  -H "Content-Type: application/json" \
  -d '{"blogId":4,"content":"Great recommendation."}'
```

## Testing

Run backend tests:

```bash
cd ShopHub-backend
mvn clean test
```

Current test coverage includes:

- phone number and verification-code validation
- cache key-prefix behavior
- Redis lock owner-token behavior
- flash-sale Lua script contract checks

Recommended next tests:

- login and interceptor behavior with MockMvc
- Redis Stream order consumption
- duplicate-order protection
- cache penetration and cache rebuild behavior

## Trade-Offs

- Redis is used for fast admission control during flash sales, but MySQL remains the final source of truth.
- Redis Streams provide lightweight async order processing without introducing an external message broker.
- The current project does not implement real payment processing, production SMS delivery, or production-grade permission roles.
- Frontend and Nginx static assets are intentionally not included in this backend-focused version.

## Roadmap

- Add Testcontainers-based MySQL and Redis integration tests
- Add OpenAPI documentation
- Add CI for build and tests
- Add rate limiting for verification-code requests
- Add a small backend-focused architecture diagram image

## License

This project is for learning and interview demonstration purposes.
