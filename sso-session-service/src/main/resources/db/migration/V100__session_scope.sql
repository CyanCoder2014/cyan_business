create table if not exists sso_sessions (
  session_id varchar(255) primary key,
  username varchar(255) not null,
  client_id varchar(255) not null,
  device_id varchar(255),
  active boolean not null,
  issued_at_epoch_second bigint not null,
  expires_at_epoch_second bigint not null
);
alter table sso_sessions add column if not exists active_tenant_key varchar(120);
alter table sso_sessions add column if not exists active_site_key varchar(120);
