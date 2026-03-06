create table if not exists processed_events (
                                                event_id varchar(100) primary key,
    processed_at timestamptz not null default now()
    );