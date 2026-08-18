create table if not exists published_forms (
  id bigserial primary key,
  slug varchar(120) not null,
  tenant_key varchar(120) not null,
  site_key varchar(120) not null default '',
  service_key varchar(120) not null,
  entity_key varchar(180) not null,
  title varchar(240) not null,
  description varchar(1000),
  visibility varchar(24) not null,
  status varchar(24) not null,
  created_by varchar(180) not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint uk_published_forms_slug unique (slug),
  constraint ck_published_forms_visibility check (visibility in ('PUBLIC', 'AUTHENTICATED')),
  constraint ck_published_forms_status check (status in ('PUBLISHED', 'ARCHIVED'))
);

create index if not exists ix_published_forms_scope
  on published_forms(tenant_key, site_key, status, updated_at desc);
