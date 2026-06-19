#!/bin/bash
# ============================================
#   Let's Encrypt Free SSL Setup
#   Usage: ./scripts/setup-ssl.sh your-domain.com your-email.com
# ============================================

set -e

DOMAIN=$1
EMAIL=$2

if [ -z "$DOMAIN" ] || [ -z "$EMAIL" ]; then
    echo "Usage: $0 <domain> <email>"
    echo "Example: $0 jxc.example.com admin@example.com"
    exit 1
fi

echo "Setting up SSL for ${DOMAIN}..."

# Install certbot if not present
if ! command -v certbot &> /dev/null; then
    echo "Installing certbot..."
    if command -v apt-get &> /dev/null; then
        sudo apt-get update && sudo apt-get install -y certbot
    elif command -v yum &> /dev/null; then
        sudo yum install -y certbot
    else
        echo "Please install certbot manually: https://certbot.eff.org/"
        exit 1
    fi
fi

# Stop nginx temporarily
docker compose stop frontend

# Get certificate
sudo certbot certonly --standalone \
    -d ${DOMAIN} \
    --email ${EMAIL} \
    --agree-tos \
    --non-interactive

# Copy certificates
sudo cp /etc/letsencrypt/live/${DOMAIN}/fullchain.pem ssl/fullchain.pem
sudo cp /etc/letsencrypt/live/${DOMAIN}/privkey.pem ssl/privkey.pem
sudo chmod 644 ssl/fullchain.pem
sudo chmod 600 ssl/privkey.pem

# Update nginx config with domain
sed -i "s/server_name _;/server_name ${DOMAIN};/" jxc-frontend/nginx.conf

# Restart
docker compose up -d frontend

echo ""
echo "SSL setup complete!"
echo "Your site is now available at: https://${DOMAIN}"
echo ""
echo "Auto-renewal is configured via certbot's systemd timer."
echo "Test renewal: sudo certbot renew --dry-run"
