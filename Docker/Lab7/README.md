# Lab 7: Multi-Network Microservices, Volumes, and Bind Mounts

## Objective
Run three Python microservices (frontend, backend, notification) in Docker with network isolation, named volumes for logs, and a bind mount for backend data.

Source code: https://github.com/Ibrahim-Adel15/MicroserviceApp.git

## Architecture

| Service | Port | Networks | Storage |
|---|---|---|---|
| frontend | 3000 (published on host 8080) | frontend-backend | none |
| backend | 5000 | frontend-backend, backend-notification | `backend-logs` volume at `/app/logs`, host folder `backend-data` at `/app/data` |
| notification | 5001 | backend-notification | `notification-logs` volume at `/app/logs` |

```text
Browser -> frontend:8080 -> backend -> notification
           [frontend-backend]  [backend-notification]
```

The frontend cannot reach the notification service directly, because they share no network. Only the backend is on both networks.

## Project Structure
```text
Lab7/
├── MicroserviceApp/
│   ├── frontend/       # Dockerfile, app.py, requirements.txt
│   ├── backend/        # Dockerfile, app.py, requirements.txt
│   └── notification/   # Dockerfile, app.py, requirements.txt
└── README.md
```

## Step 1: Build the images
```bash
docker build -t notification ./notification
docker build -t backend ./backend
docker build -t frontend ./frontend
```

## Step 2: Create networks, volumes and the host directory
```bash
docker network create --driver bridge --subnet 172.20.0.0/24 frontend-backend
docker network create --driver bridge --subnet 172.21.0.0/24 backend-notification

docker volume create backend-logs
docker volume create notification-logs

mkdir -p backend-data
```

## Step 3: Run the containers
```bash
# Notification: backend-notification network only
docker run -d --name notification --network backend-notification \
  -v notification-logs:/app/logs \
  notification

# Backend: both networks, notification URL, log volume, bind mount
docker run -d --name backend --network backend-notification \
  -e NOTIFICATION_URL=http://notification:5001/notify \
  -v backend-logs:/app/logs \
  -v "$PWD/backend-data:/app/data" \
  backend
docker network connect frontend-backend backend

# Frontend: frontend-backend network, published on port 8080
docker run -d --name frontend --network frontend-backend -p 8080:3000 \
  -e BACKEND_URL=http://backend:5000 \
  frontend
```

`docker run` accepts only one `--network`, so the backend starts on `backend-notification` and `docker network connect` adds `frontend-backend`. Containers on a user-defined network reach each other by container name.

## Step 4: Verify

**Containers and networks**
```bash
docker ps
docker network inspect frontend-backend backend-notification \
  --format '{{.Name}} {{(index .IPAM.Config 0).Subnet}}: {{range .Containers}}{{.Name}} {{end}}'
```
`frontend-backend` contains `frontend` and `backend`. `backend-notification` contains `notification` and `backend`.

**Network isolation**
```bash
docker exec frontend python -c "import socket;print(socket.gethostbyname('backend'))"
docker exec