CREATE TABLE IF NOT EXISTS user_notifications (
  notification_id VARCHAR(160) PRIMARY KEY,
  tenant_key VARCHAR(160) NOT NULL,
  site_key VARCHAR(160),
  recipient VARCHAR(255) NOT NULL,
  type VARCHAR(80) NOT NULL,
  severity VARCHAR(40) NOT NULL,
  title VARCHAR(500) NOT NULL,
  body VARCHAR(4000),
  deep_link VARCHAR(1000),
  source_service VARCHAR(160),
  source_key VARCHAR(255),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  read_at TIMESTAMP WITH TIME ZONE,
  version BIGINT NOT NULL DEFAULT 1
);
CREATE INDEX IF NOT EXISTS idx_notification_recipient_scope ON user_notifications(recipient, tenant_key, site_key, created_at);
