create type detection_kind as enum ('HUMAN', 'AI_GENERATED');
create table history_records (
    id uuid primary key,
    input_type varchar(16) not null,
    media_type varchar(16),
    kind detection_kind not null,
    accuracy double precision not null check (accuracy >= 0.0 and accuracy <= 1.0),
    created_at timestamptz not null
);
