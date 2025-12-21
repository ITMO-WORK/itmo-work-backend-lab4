create table if not exists application_registry (
    application_id uuid primary key,
    owner_id       uuid not null,
    created_at     timestamptz default now()
);