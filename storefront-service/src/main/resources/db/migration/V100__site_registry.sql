create table if not exists sites (
  id bigserial primary key,
  tenant_key varchar(120) not null,
  site_key varchar(120) not null,
  name varchar(200) not null,
  status varchar(24) not null,
  created_at timestamp with time zone not null,
  constraint uk_site_tenant_key unique (tenant_key, site_key)
);
create table if not exists site_idempotency (
  id bigserial primary key,
  actor varchar(180) not null,
  idempotency_key varchar(180) not null,
  site_key varchar(120) not null,
  created_at timestamp with time zone not null,
  constraint uk_site_idempotency unique (actor, idempotency_key)
);
