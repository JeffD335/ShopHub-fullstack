# Free Cloud Deployment

This guide deploys ShopHub on one free-tier VPS with Docker Compose. It is the simplest free path for an interview demo because the frontend, backend, MySQL, and Redis run together on one machine.

## Why This Path

ShopHub needs four runtime pieces:

- React frontend served by Nginx
- Spring Boot backend
- MySQL
- Redis

Many free app platforms sleep after inactivity or have very small memory limits. A free VPS is easier to explain and easier to debug because it behaves like a normal Linux server.

## Recommended Free Provider

Use Oracle Cloud Always Free with an Ubuntu VM.

Recommended VM shape:

- Ampere A1 ARM VM
- 1-2 OCPU
- 6-12 GB RAM
- Ubuntu 22.04 or 24.04

Open inbound ports in the cloud firewall:

- `22` for SSH
- `80` for the web app

Do not open MySQL `3306` or Redis `6379` to the public internet.

Official reference: <https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm>

## Step 1: SSH Into The Server

From your laptop:

```bash
ssh ubuntu@YOUR_SERVER_IP
```

Replace `YOUR_SERVER_IP` with the public IP shown in the cloud console.

## Step 2: Install Docker

On the server:

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl git
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker
docker version
docker compose version
```

What this does:

- Docker runs the app pieces in containers.
- Docker Compose starts all containers with one command.
- `usermod -aG docker $USER` lets your current Linux user run Docker without typing `sudo` every time.

## Step 3: Download The Project

```bash
git clone -b fullstack-frontend https://github.com/JeffD335/ShopHub-fullstack.git
cd ShopHub-fullstack
```

Use the branch that contains the full-stack demo work.

## Step 4: Create The Environment File

```bash
cp .env.example .env
nano .env
```

Change this value:

```text
MYSQL_ROOT_PASSWORD=change_this_to_a_long_random_password
```

Example:

```text
MYSQL_ROOT_PASSWORD=ShopHub_Cloud_Demo_2026_ChangeMe
```

The real `.env` file is ignored by Git because it contains secrets.

## Step 5: Start ShopHub

```bash
docker compose -f docker-compose.free.yml up -d --build
```

What this does:

- Builds the React frontend image
- Builds the Spring Boot backend image
- Starts MySQL and imports the seed SQL file
- Starts Redis
- Publishes only the frontend on port `80`

## Step 6: Check The App

```bash
docker compose -f docker-compose.free.yml ps
curl http://localhost/api/health
```

Expected health response:

```json
{"success":true,"errorMsg":null,"data":"OK","total":null}
```

Then open this in a browser:

```text
http://YOUR_SERVER_IP
```

## Useful Commands

View logs:

```bash
docker compose -f docker-compose.free.yml logs -f
```

Restart:

```bash
docker compose -f docker-compose.free.yml restart
```

Stop:

```bash
docker compose -f docker-compose.free.yml down
```

Update after pushing new code:

```bash
git pull
docker compose -f docker-compose.free.yml up -d --build
```

## Important Notes

- The cloud deployment file does not publish MySQL or Redis ports.
- MySQL data, Redis data, and uploaded images are stored in Docker volumes.
- The first build can take several minutes on a free VM.
- If the browser cannot open the site, check the cloud firewall first, then check `docker compose -f docker-compose.free.yml ps`.
- Oracle free capacity can be unavailable in some regions. If VM creation fails, try another region or retry later.
