create table billing_plans (
    plan_key varchar(80) primary key,
    display_name varchar(180) not null,
    description varchar(1000),
    billing_mode varchar(24) not null,
    active boolean not null,
    features_json varchar(12000) not null,
    limits_json varchar(12000) not null
);

create table tenant_subscriptions (
    tenant_key varchar(80) primary key,
    plan_key varchar(80) not null,
    status varchar(24) not null,
    started_at timestamp not null,
    renews_at timestamp,
    updated_at timestamp not null,
    constraint fk_subscription_plan foreign key (plan_key) references billing_plans(plan_key)
);

create table billing_idempotency_records (
    record_id varchar(500) primary key,
    tenant_key varchar(80) not null,
    created_at timestamp not null
);
