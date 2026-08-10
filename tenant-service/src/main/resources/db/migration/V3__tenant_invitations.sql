create table tenant_invitations (
    invitation_id varchar(64) primary key,
    tenant_key varchar(80) not null references platform_tenants(tenant_key),
    email varchar(240) not null,
    role_key varchar(64) not null,
    token_hash varchar(64) not null unique,
    status varchar(24) not null,
    delivery_status varchar(32) not null,
    created_by varchar(180) not null,
    created_at timestamp not null,
    expires_at timestamp not null,
    accepted_at timestamp
);
create index idx_tenant_invitation_scope on tenant_invitations(tenant_key,created_at);
