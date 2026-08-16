create table if not exists billing_idempotency_records (
    record_id varchar(500) primary key,
    tenant_key varchar(80) not null,
    created_at timestamp not null
);
