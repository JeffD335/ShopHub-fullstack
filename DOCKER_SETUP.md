# Docker Setup

This project can be started with Docker Compose from the repository root.

## Start

```bash
docker compose up --build
```

For older Docker Compose installations:

```bash
docker-compose up --build
```

## Services

- Frontend app: `http://localhost:8080`
- Backend API: `http://localhost:8081`
- MySQL: `localhost:3306`
- Redis: `localhost:6379`

The frontend container serves the React build and proxies `/api/*` requests to the backend container.

The backend runs with the `docker` Spring profile. In that profile, MySQL is reached through the Compose service name `mysql`, and Redis is reached through the service name `redis`.

## Configuration

The default database password is intended only for local development and can be overridden:

```bash
MYSQL_ROOT_PASSWORD=your_password docker compose up --build
```

The backend receives the password through `SPRING_DATASOURCE_PASSWORD`.

## Stop

```bash
docker compose down
```

To also delete local database and Redis volumes:

```bash
docker compose down -v
```

## Useful Commands

View logs:

```bash
docker compose logs -f
```

Rebuild only the backend:

```bash
docker compose build backend
```

Open a MySQL shell:

```bash
docker compose exec mysql mysql -uroot -p ShopHub
```

Open a Redis shell:

```bash
docker compose exec redis redis-cli
```
