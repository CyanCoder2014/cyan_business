create table tenant_roles (
    role_id varchar(180) primary key,
    tenant_key varchar(80) not null,
    role_key varchar(64) not null,
    display_name varchar(120) not null,
    description varchar(400),
    system_role boolean not null,
    revision bigint not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_tenant_role_tenant foreign key (tenant_key) references platform_tenants(tenant_key),
    constraint uq_tenant_role unique (tenant_key, role_key)
);

create table tenant_role_permissions (
    role_id varchar(180) not null,
    permission_key varchar(120) not null,
    constraint fk_tenant_role_permission foreign key (role_id) references tenant_roles(role_id),
    constraint uq_tenant_role_permission unique (role_id, permission_key)
);

alter table tenant_memberships add column updated_at timestamp;
update tenant_memberships set updated_at = created_at where updated_at is null;
alter table tenant_memberships alter column updated_at set not null;
