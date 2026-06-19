#!/bin/bash
# ============================================
#   JXC Management System - Production Deploy
#   Usage: chmod +x deploy.sh && ./deploy.sh
# ============================================

set -e

echo "============================================"
echo "  JXC Production Deployment"
echo "============================================"
echo ""

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "[ERROR] Docker not installed!"
    echo "Install: curl -fsSL https://get.docker.com | sh"
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "[ERROR] Docker Compose not installed!"
    exit 1
fi

# Check .env
if [ ! -f .env ]; then
    echo "[ERROR] .env file not found!"
    echo "Copy .env.example to .env and fill in your values."
    exit 1
fi

# Source .env
source .env

# Validate required variables
if [ -z "$DB_PASSWORD" ] || [ "$DB_PASSWORD" = "changeme" ]; then
    echo "[ERROR] DB_PASSWORD must be set in .env (not 'changeme')"
    exit 1
fi

if [ -z "$JWT_SECRET" ] || [ ${#JWT_SECRET} -lt 32 ]; then
    echo "[ERROR] JWT_SECRET must be at least 32 characters in .env"
    exit 1
fi

# Create directories
mkdir -p ssl backups nginx-conf logs

# Generate self-signed SSL cert (for testing, replace with Let's Encrypt for production)
DOMAIN="${DOMAIN:-localhost}"
if [ ! -f ssl/fullchain.pem ]; then
    echo "[1/5] Generating self-signed SSL certificate..."
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout ssl/privkey.pem \
        -out ssl/fullchain.pem \
        -subj "/CN=${DOMAIN}" 2>/dev/null
    echo "  Self-signed cert created for ${DOMAIN}"
    echo "  For production, use Let's Encrypt: see setup-ssl.sh"
else
    echo "[1/5] SSL certificate already exists"
fi

# Copy nginx config
echo "[2/5] Configuring nginx..."
cp jxc-frontend/nginx.conf nginx-conf/default.conf

# Build and start
echo "[3/5] Building Docker images..."
docker compose build --no-cache

echo "[4/5] Starting services..."
docker compose up -d

# Wait for services
echo "[5/5] Waiting for services to start..."
sleep 10

# Health check
for i in $(seq 1 30); do
    if curl -sf http://localhost:8080/api/auth/login > /dev/null 2>&1; then
        echo ""
        echo "============================================"
        echo "  Deployment Successful!"
        echo "============================================"
        echo "  Frontend: https://${DOMAIN}"
        echo "  Backend:  http://localhost:8080"
        echo "  Username: admin"
        echo "  Password: admin123"
        echo ""
        echo "  IMPORTANT: Change admin password immediately!"
        echo "============================================"
        exit 0
    fi
    echo "  Waiting... ($i/30)"
    sleep 3
done

echo "[ERROR] Services did not start in time. Check: docker compose logs"
exit 1
