create table if not exists media_reference (
 reference_id varchar(64) primary key, tenant_key varchar(200) not null, site_key varchar(200), asset_key varchar(64) not null,
 owner_service varchar(100) not null, owner_type varchar(100) not null, owner_key varchar(200) not null, field_path varchar(300) not null,
 created_at timestamp with time zone not null,
 constraint uk_media_reference_owner unique(tenant_key,site_key,asset_key,owner_service,owner_type,owner_key,field_path)
);
create index if not exists idx_media_reference_asset on media_reference(tenant_key,site_key,asset_key);
