create table if not exists stored_files (
    id uuid primary key default gen_random_uuid(),
    owner_id uuid not null,
    entity_type text not null,
    entity_id uuid not null,
    purpose text not null,
    bucket text not null,
    object_key text not null,
    original_file_name text not null,
    content_type text not null,
    size_bytes bigint not null,
    created_at timestamptz default now()
);