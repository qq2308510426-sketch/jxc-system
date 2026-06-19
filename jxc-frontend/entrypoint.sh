#!/bin/sh
BACKEND_HOST=${BACKEND_HOST:-jxc-system-production.up.railway.app}
sed -i "s/BACKEND_HOST/${BACKEND_HOST}/g" /etc/nginx/conf.d/default.conf
exec nginx -g "daemon off;"