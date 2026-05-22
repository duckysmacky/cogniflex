create table analysis_cache_records (
    id uuid primary key,
    input_type varchar(16) not null,
    media_type varchar(16),
    content_hash varchar(64) not null,
    hash_algorithm varchar(32) not null,
    kind integer not null check (kind in (0, 1)),
    accuracy double precision not null check (accuracy >= 0.0 and accuracy <= 1.0),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create unique index ux_analysis_cache_records_lookup on analysis_cache_records (
    input_type,
    coalesce(media_type, 'NONE'),
    hash_algorithm,
    content_hash
);

create index ix_analysis_cache_records_content_hash on analysis_cache_records (content_hash);
