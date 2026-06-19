#!/bin/bash
# JXC Database Backup Script
# Run daily via cron: 0 2 * * * /path/to/scripts/backup-db.sh

BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)
KEEP_DAYS=7

# Create backup
mysqldump -h mysql -u root -p"${DB_PASSWORD}" jxc_db > "${BACKUP_DIR}/jxc_db_${DATE}.sql"

# Compress
gzip "${BACKUP_DIR}/jxc_db_${DATE}.sql"

# Clean old backups
find "${BACKUP_DIR}" -name "jxc_db_*.sql.gz" -mtime +${KEEP_DAYS} -delete

echo "[$(date)] Backup completed: jxc_db_${DATE}.sql.gz"
