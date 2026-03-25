create table history_records (
    id uuid primary key,
    type varchar(16) not null,
    kind smallint not null check (kind in (0, 1)),
    accuracy double precision not null check (accuracy >= 0.0 and accuracy <= 1.0),
    created_at timestamptz not null
);
