#!/bin/bash
set -e

compose() {
    if docker compose version > /dev/null 2>&1; then
        docker compose "$@"
    elif command -v docker-compose > /dev/null 2>&1; then
        docker-compose "$@"
    else
        echo "Docker Compose is not installed."
        exit 1
    fi
}

compose down

echo "ShopHub services stopped."
echo "To also remove local data volumes, run: docker compose down -v"
