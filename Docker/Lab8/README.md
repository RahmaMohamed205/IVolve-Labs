# Lab 8: Containerized Node.js and MySQL Stack Using Docker Compose

## Objective
Run a Node.js application and a MySQL database with Docker Compose, persist logs and database data, and push the application image to Docker Hub.

Source code: https://github.com/Ibrahim-Adel15/iVolveApp.git

## Architecture

| Service | Image | Port | Volume | Network |
|---|---|---|---|---|
| app | built from `Dockerfile` (`rahmaelballat/ivolve-app:v1`) | 3000 | `app_logs` -> `/app/logs/` | ivolve-net |
| db | `mysql:8.0` | internal | `db_data` -> `/var/lib/mysql` | ivolve-net |

The application needs a MySQL connection and a database named `ivolve`. It reads `DB_HOST`, `DB_USER`, `DB_PASSWORD` and `DB_NAME`, and retries until the database exists.

## Project Structure
```text
Lab8/
├── iVolveApp/
│   ├── docker-compose.yml
│   ├── Dockerfile
│   ├── server.js
│   ├── db.js
│   ├── package.json
│   └── frontend/
└── README.md
```

## docker-compose.yml
```yaml
services:
  app:
    build: .
    image: ${DOCKERHUB_USER}/ivolve-app:v1
    container_name: ivolve-app
    ports:
      - "3000:3000"
    environment:
      DB_HOST: db
      DB_USER: root
      DB_PASSWORD: rootpass
      DB_NAME: ivolve
    volumes:
      - app_logs:/app/logs/
    networks:
      - ivolve-net
    depends_on:
      db:
        condition: service_healthy

  db:
    image: mysql:8.0
    container_name: ivolve-db
    environment:
      MYSQL_ROOT_PASSWORD: rootpass
      MYSQL_DATABASE: ivolve
    volumes:
      - db_data:/var/lib/mysql
    networks:
      - ivolve-net
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "-uroot", "-prootpass"]
      interval: 5s
      timeout: 5s
      retries: 20

networks:
  ivolve-net:

volumes:
  app_logs:
  db_data:
```

- `depends_on` with a healthcheck makes the app wait until MySQL accepts connections.
- `MYSQL_DATABASE: ivolve` creates the database the application requires.

## Step 1: Start the services
```bash
export DOCKERHUB_USER=rahmaelballat
docker compose up -d --build
docker compose ps
docker compose logs app
```
`ivolve-db` becomes `healthy`, then `ivolve-app` starts and connects to the database.

## Step 2: Verify the application and the database
```bash
curl -i http://localhost:3000
docker exec ivolve-db mysql -uroot -prootpass -e "SHOW DATABASES;"
docker exec ivolve-app ls /app/logs
docker volume ls | grep -E "app_logs|db_data"
docker network ls | grep ivolve-net
```
- The application responds on http://localhost:3000.
- The database `ivolve` exists.
- Logs are written to `/app/logs`.

## Step 3: Data persistence
```bash
docker compose down
docker compose up -d
docker exec ivolve-db mysql -uroot -prootpass -e "SHOW DATABASES;"
```
The `ivolve` database is still present after the containers are recreated, because `db_data` is a named volume. `docker compose down -v` would delete it.

## Step 4: Push the image to Docker Hub
```bash
docker login -u rahmaelballat
docker images | grep ivolve-app
docker push rahmaelballat/ivolve-app:v1
```
Image: https://hub.docker.com/r/rahmaelballat/ivolve-app

## Requirements Checklist

| Requirement | Result |
|---|---|
| Clone the application | Done |
| App built from Dockerfile, port 3000 | Done |
| Database environment variables for the app | `DB_HOST`, `DB_USER`, `DB_PASSWORD`, `DB_NAME` |
| App logs persisted at `/app/logs/` | `app_logs` volume |
| MySQL with required environment variables | `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE` |
| Database data persisted with `db_data` at `/var/lib/mysql` | Done |
| Custom network `ivolve-net` | Done |
| Services started, app and DB connectivity verified | Done |
| Image tagged with Docker Hub username and pushed | `rahmaelballat/ivolve-app:v1` |

## Notes
- The password in the compose file is for the lab only. In real projects, use an `.env` file or secrets and keep them out of Git.
- Compose adds a project prefix to volume and network names (for example `iVolveApp_db_data`).