create table if not exists media_upload (
    upload_id varchar(64) primary key,
    asset_key varchar(64) not null unique,
    tenant_key varchar(200) not null,
    site_key varchar(200),
    original_file_name varchar(512) not null,
    mime_type varchar(255) not null,
    visibility varchar(32) not null,
    expected_size_bytes bigint not null,
    uploaded_size_bytes bigint not null default 0,
    status varchar(32) not null,
    created_by varchar(255) not null,
    created_at timestamp with time zone not null,
    expires_at timestamp with time zone not null,
    completed_at timestamp with time zone,
    storage_path varchar(2048)
);
create index if not exists idx_media_upload_scope on media_upload(tenant_key, site_key, created_at desc);
