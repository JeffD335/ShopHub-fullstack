# ShopHub - Local Life Service Platform

## Project Overview

ShopHub is a Spring Boot-based local life service platform, similar to Yelp or Dianping. This project implements a complete system including user management, shop management, voucher system, flash sale functionality, and blog community features, demonstrating technical solutions for high-concurrency scenarios.

## Technology Stack

### Backend Technologies
- **Framework**: Spring Boot 2.3.12
- **Database**: MySQL 5.7+
- **Cache**: Redis (Lettuce Connection Pool)
- **ORM**: MyBatis-Plus 3.4.3
- **Distributed Lock**: Redisson 3.13.6
- **Utility Library**: Hutool 5.7.17
- **Others**: Lombok, Spring AOP

### Frontend Technologies
- **Static Pages**: HTML + CSS + JavaScript
- **Framework**: Vue.js, Element UI
- **HTTP Client**: Axios

### Deployment
- **Web Server**: Nginx 1.18.0

## Core Features

### 1. User System
- User registration and login
- Redis-based session management
- Token refresh mechanism
- User profile management

### 2. Shop System
- Shop category management
- Shop list query (with pagination support)
- Shop detail display
- Redis-based cache optimization

### 3. Voucher System
- Regular voucher management
- Flash sale voucher functionality
- Voucher query and redemption

### 4. Flash Sale System
- **Lua Script** for atomic inventory deduction
- **Distributed Lock** (Redisson) to prevent overselling
- **Optimistic Lock** to ensure data consistency
- **Redis Stream** for asynchronous order processing
- Duplicate order prevention mechanism

### 5. Blog System
- Blog publishing and editing
- Blog list (with scroll pagination)
- Blog detail display
- Blog comment functionality
- Like functionality

### 6. Social Features
- User follow/unfollow
- Follow list query
- Common follow functionality

## Technical Highlights

### 1. High Concurrency Optimization
- **Redis Cache**: Hot data caching to reduce database pressure
- **Lua Script**: Ensures atomicity of inventory deduction
- **Distributed Lock**: Uses Redisson to implement distributed locks, preventing concurrency issues
- **Optimistic Lock**: Database-level data consistency guarantee

### 2. Flash Sale System Design
- Uses Lua script to complete inventory check and deduction in Redis, ensuring atomicity
- Records ordered users through Redis Set to prevent duplicate orders
- Uses Redis Stream for asynchronous order processing to improve system throughput
- Database-level optimistic lock further ensures inventory accuracy

### 3. Architecture Design
- **Layered Architecture**: Controller → Service → Mapper
- **Unified Exception Handling**: Global exception capture and processing
- **Interceptor**: Token refresh interceptor to automatically maintain user login status
- **AOP**: Uses Spring AOP to obtain proxy objects

### 4. Code Quality
- Uses Lombok to simplify code
- Unified response result encapsulation (Result)
- Standardized package structure design

## Project Structure

```
ShopHub/
├── ShopHub-backend/          # Backend project
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/shopHub/
│   │   │   │   ├── config/          # Configuration classes
│   │   │   │   ├── controller/      # Controllers
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── entity/          # Entity classes
│   │   │   │   ├── mapper/          # MyBatis Mappers
│   │   │   │   ├── service/         # Service layer
│   │   │   │   └── utils/           # Utility classes
│   │   │   └── resources/
│   │   │       ├── application.yaml # Configuration file
│   │   │       ├── db/              # SQL scripts
│   │   │       └── *.lua            # Lua scripts
│   └── pom.xml
├── nginx-1.18.0/             # Nginx configuration
│   └── html/hmdp/            # Frontend static files
└── README_EN.md
```

## Quick Start

### Requirements
- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- Redis 5.0+
- Nginx 1.18+ (Optional)

### Installation Steps

1. **Clone the project**
```bash
git clone <repository-url>
cd ShopHub
```

2. **Database Configuration**
   - Create database `ShopHub`
   - Execute `ShopHub-backend/src/main/resources/db/hmdp.sql` to initialize the database

3. **Modify Configuration**
   Edit `ShopHub-backend/src/main/resources/application.yaml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/ShopHub?useSSL=false&serverTimezone=UTC
       username: your_username
       password: your_password
     redis:
       host: localhost
       port: 6379
   ```

4. **Start Redis**
```bash
redis-server
```

5. **Start Backend Service**
```bash
cd ShopHub-backend
mvn clean install
mvn spring-boot:run
```

6. **Start Nginx** (Optional)
```bash
cd nginx-1.18.0
./nginx  # Linux/Mac
# or nginx.exe  # Windows
```

7. **Access the Application**
   - Frontend: http://localhost:8080
   - Backend API: http://localhost:8081

## API Documentation

### User Related
- `POST /user/code` - Send verification code
- `POST /user/login` - User login
- `GET /user/me` - Get current user information
- `POST /user/logout` - User logout

### Shop Related
- `GET /shop-type/list` - Get shop categories
- `GET /shop/{id}` - Get shop details
- `GET /shop/of/type` - Query shops by type

### Voucher Related
- `GET /voucher/list/{shopId}` - Query shop vouchers
- `POST /voucher` - Add regular voucher
- `POST /voucher/seckill` - Add flash sale voucher
- `POST /voucher-order/seckill/{id}` - Flash sale order

### Blog Related
- `GET /blog/{id}` - Get blog details
- `POST /blog` - Publish blog
- `GET /blog/of/follow` - Get blogs from followed users
- `POST /blog/like/{id}` - Like blog

## Performance Optimization

1. **Caching Strategy**
   - Cache hot data (shop information, user information) in Redis
   - Set reasonable expiration times to avoid cache avalanche

2. **Database Optimization**
   - Use indexes to optimize query performance
   - Pagination queries to avoid full table scans

3. **Concurrency Control**
   - Lua script ensures atomic operations
   - Distributed locks prevent concurrency issues
   - Optimistic locks ensure data consistency

## Future Plans

- [ ] Introduce message queue (RabbitMQ/Kafka) for asynchronous processing optimization
- [ ] Implement distributed session management
- [ ] Add Elasticsearch for full-text search
- [ ] Introduce Spring Cloud for microservices architecture
- [ ] Add unit tests and integration tests
- [ ] Implement API rate limiting and circuit breaker mechanism

## Author

Jianfei Dou

## License

This project is for learning and interview demonstration purposes only.
