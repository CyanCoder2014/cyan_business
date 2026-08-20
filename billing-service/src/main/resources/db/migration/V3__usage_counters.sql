create table if not exists tenant_usage_counters (
    tenant_key varchar(80) not null,
    metric_key varchar(80) not null,
    counter_value bigint not null default 0,
    updated_at timestamp not null,
    primary key (tenant_key, metric_key)
);
