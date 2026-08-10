create table platform_tenants (
    tenant_key varchar(80) primary key,
    display_name varchar(180) not null,
    status varchar(24) not null,
    created_by varchar(180) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table tenant_memberships (
    membership_id varchar(280) primary key,
    tenant_key varchar(80) not null,
    username varchar(180) not null,
    role_key varchar(64) not null,
    active boolean not null,
    created_at timestamp not null,
    constraint fk_tenant_membership_tenant foreign key (tenant_key) references platform_tenants(tenant_key),
    constraint uq_tenant_membership unique (tenant_key, username)
);

create table tenant_capability_overrides (
    override_id varchar(260) primary key,
    tenant_key varchar(80) not null,
    site_key varchar(80),
    capability_key varchar(80) not null,
    enabled boolean not null,
    reason varchar(400),
    constraint fk_tenant_capability_tenant foreign key (tenant_key) references platform_tenants(tenant_key)
);

create table tenant_feature_flags (
    flag_id varchar(220) primary key,
    tenant_key varchar(80) not null,
    flag_key varchar(100) not null,
    flag_value varchar(1000) not null,
    constraint fk_tenant_flag_tenant foreign key (tenant_key) references platform_tenants(tenant_key),
    constraint uq_tenant_flag unique (tenant_key, flag_key)
);

create table tenant_idempotency_records (
    record_id varchar(500) primary key,
    resource_key varchar(180) not null,
    created_at timestamp not null
);
