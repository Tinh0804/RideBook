# RideBook Production Deployment Guide

This directory contains the production deployment configuration for RideBook using Docker Compose and Nginx reverse proxy.

## 1. Directory Structure

```text
deploy/
  ├── nginx/
  │   ├── conf.d/
  │   │   └── api.conf        # API reverse proxy, WebSocket & SSL routing rules
  │   └── nginx.conf          # Master Nginx configuration (worker, gzip, logs)
  ├── scripts/
  │   ├── deploy.sh           # Deploy an immutable Backend image and verify health
  │   └── rollback.sh         # Roll back to the previously healthy image
  ├── .env.example            # Template only; never used directly by production
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

### Step 1: Provision the Runtime Environment Once

Production has one runtime configuration source of truth:
`/etc/ridebook/ridebook.env`. Deployment workflows do not create or overwrite
this file. Install it once from the template, then replace every placeholder
with the real production value:

```bash
sudo groupadd --force ridebook-deploy
sudo usermod -aG ridebook-deploy "$USER"
sudo install -d -o root -g ridebook-deploy -m 750 /etc/ridebook
sudo install -d -o "$USER" -g ridebook-deploy -m 750 \
  /opt/ridebook /opt/ridebook/releases /opt/ridebook/state
sudo install -o root -g ridebook-deploy -m 640 \
  deploy/.env.example /etc/ridebook/ridebook.env
sudoedit /etc/ridebook/ridebook.env
deploy/scripts/validate-runtime-env.sh /etc/ridebook/ridebook.env
```

Start a new SSH session after adding the deployment user to the group. Edit the
runtime file with actual production values:

- `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`: PostgreSQL database credentials.
- `JWT_SIGNER_KEY`: Secure random 256-bit string.
- `DOMAIN`, `FRONTEND_URL`, `CORS_ALLOWED_ORIGINS`: Your production domain names.
- OAuth2, VNPay, MoMo, Firebase, and Google Maps credentials.

The deployed Backend digest is intentionally stored separately in
`/opt/ridebook/state/release.env`; never add `BACKEND_IMAGE` or `IMAGE_TAG` to
the runtime environment file.

### Step 2: SSL Certificate Setup

#### Option A: Initial Let's Encrypt Certificate via Certbot Standalone
If obtaining the certificate for the first time before starting Nginx:

```bash
sudo certbot certonly --standalone -d api.yourdomain.com
```

Ensure certificate paths in the deployment Nginx configuration match your
Let's Encrypt live path.

### Step 3: Start Services

Production deployment is owned by `.github/workflows/release-backend.yml`. It
builds one image, captures its immutable digest, waits for the GitHub production
environment approval, and invokes `.github/workflows/deploy-production.yml`.

For an authorized manual verification on the VM, use both environment files:

```bash
docker compose \
  --env-file /etc/ridebook/ridebook.env \
  --env-file /opt/ridebook/state/release.env \
  -f /opt/ridebook/releases/current/compose.prod.yml \
  config --quiet
```

### Step 4: Verify Health

Check container status and health checks:

```bash
docker compose \
  --env-file /etc/ridebook/ridebook.env \
  --env-file /opt/ridebook/state/release.env \
  -f /opt/ridebook/releases/current/compose.prod.yml \
  ps
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
compose=(docker compose
  --env-file /etc/ridebook/ridebook.env
  --env-file /opt/ridebook/state/release.env
  -f /opt/ridebook/releases/current/compose.prod.yml)

# View all service logs
"${compose[@]}" logs -f

# View backend logs specifically
"${compose[@]}" logs -f backend

# View Nginx access & error logs
"${compose[@]}" logs -f nginx
```

### Release a New Backend Version

Create an approved `v*` tag or manually run the `Release Backend` workflow.
Do not run the legacy `CD Pipeline`; it is retained only as a disabled migration
notice.

### Manual Rollback

```bash
/opt/ridebook/releases/current/scripts/rollback.sh
```

### Stop / Restart Services
```bash
# Restart all services
docker compose \
  --env-file /etc/ridebook/ridebook.env \
  --env-file /opt/ridebook/state/release.env \
  -f /opt/ridebook/releases/current/compose.prod.yml restart

# Stop all services gracefully
docker compose \
  --env-file /etc/ridebook/ridebook.env \
  --env-file /opt/ridebook/state/release.env \
  -f /opt/ridebook/releases/current/compose.prod.yml stop

# Stop and remove containers (preserves named volumes)
docker compose \
  --env-file /etc/ridebook/ridebook.env \
  --env-file /opt/ridebook/state/release.env \
  -f /opt/ridebook/releases/current/compose.prod.yml down
```
