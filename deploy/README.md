# RideBook Production Deployment Guide

This directory contains the production deployment configuration for RideBook using Docker Compose and Nginx reverse proxy.

## 1. Directory Structure

```text
deploy/
  ├── nginx/
  │   ├── conf.d/
  │   │   └── api.conf        # API reverse proxy, WebSocket & SSL routing rules
  │   └── nginx.conf          # Master Nginx configuration (worker, gzip, logs)
  ├── .env.example            # Template for all production environment variables
  ├── compose.prod.yml        # Docker Compose configuration for production services
  └── README.md               # Deployment instructions and operational manual
```

---

## 2. Architecture & Resource Limits

The production stack runs 4 coordinated containers:

| Component | Image / Base | Memory Limit | CPU Limit | Exposure |
|---|---|---|---|---|
| **Nginx** | `nginx:alpine` | 128 MB | 0.25 | Ports `80`, `443` (Public) |
| **Backend** | `ridebook-backend` (digest/tag) | 1400 MB (Java Heap: 896 MB) | 1.50 | Port `8080` (Internal `edge` & `data` networks) |
| **PostgreSQL** | `postgres:15.18-alpine` | 768 MB (shm: 128 MB) | - | Port `5432` (Internal `data` network only) |
| **Redis** | `redis:7.4-alpine` | 320 MB (256 MB maxmemory) | - | Port `6379` (Internal `data` network only) |

---

## 3. Prerequisites

- Docker Engine 24+ and Docker Compose plugin (`docker compose`).
- Registered Domain pointing to the server's public IP address.
- SSL Certificate (Let's Encrypt / Certbot or custom SSL certificates).

---

## 4. Deployment Steps

### Step 1: Prepare Environment Configuration
Navigate to the `deploy/` directory, create your `.env` file from the template, and configure your secrets:

```bash
cd deploy
cp .env.example .env
chmod 600 .env
```

Edit `.env` with actual production values:
- `BACKEND_IMAGE`: Docker Hub image tag or SHA256 digest (e.g. `tinh08042005/ridebook-backend:latest`).
- `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`: PostgreSQL database credentials.
- `JWT_SIGNER_KEY`: Secure random 256-bit string.
- `DOMAIN`, `FRONTEND_URL`, `CORS_ALLOWED_ORIGINS`: Your production domain names.
- OAuth2, VNPay, MoMo, Firebase, and Google Maps credentials.

### Step 2: SSL Certificate Setup

#### Option A: Initial Let's Encrypt Certificate via Certbot Standalone
If obtaining the certificate for the first time before starting Nginx:

```bash
sudo certbot certonly --standalone -d api.yourdomain.com
```

Ensure certificate paths in `.env` and `/etc/nginx/conf.d/api.conf` match your Let's Encrypt live path (default: `/etc/letsencrypt/live/api.yourdomain.com/fullchain.pem`).

### Step 3: Start Services

Validate the Compose configuration and launch all containers in detached mode:

```bash
docker compose -f compose.prod.yml config
docker compose -f compose.prod.yml up -d
```

### Step 4: Verify Health

Check container status and health checks:

```bash
docker compose -f compose.prod.yml ps
```

All 4 services should report `healthy` or `running`.

You can also test the Nginx health endpoint:
```bash
curl -i http://localhost/nginx-health
```

---

## 5. Operations & Maintenance

### View Logs
```bash
# View all service logs
docker compose -f compose.prod.yml logs -f

# View backend logs specifically
docker compose -f compose.prod.yml logs -f backend

# View Nginx access & error logs
docker compose -f compose.prod.yml logs -f nginx
```

### Update Backend to New Version
```bash
# Set new image in .env or via environment variable
docker compose -f compose.prod.yml pull backend
docker compose -f compose.prod.yml up -d --no-deps backend
```

### Stop / Restart Services
```bash
# Restart all services
docker compose -f compose.prod.yml restart

# Stop all services gracefully
docker compose -f compose.prod.yml stop

# Stop and remove containers (preserves named volumes)
docker compose -f compose.prod.yml down
```
