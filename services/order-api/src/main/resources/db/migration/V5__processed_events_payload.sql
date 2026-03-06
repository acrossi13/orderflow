alter table processed_events
    add column if not exists occurred_at timestamptz,
    add column if not exists payload jsonb;