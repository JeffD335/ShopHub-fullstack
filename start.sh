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

if ! docker info > /dev/null 2>&1; then
    echo "Docker is not running. Start Docker and try again."
    exit 1
fi

echo "Building backend image..."
compose build backend

echo "Starting services..."
compose up -d mysql redis backend

echo "Waiting for services..."
sleep 10

echo "Service status:"
compose ps

echo ""
echo "ShopHub is running."
echo "Backend API: http://localhost:8081"
echo ""
echo "View logs: docker compose logs -f"
echo "Stop services: ./stop.sh"
