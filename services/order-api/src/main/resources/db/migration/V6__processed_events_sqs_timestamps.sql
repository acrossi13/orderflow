alter table processed_events
    add column if not exists sqs_sent_at timestamptz,
    add column if not exists sqs_received_at timestamptz;

create index if not exists idx_processed_events_sqs_sent_at
    on processed_events (sqs_sent_at);

create index if not exists idx_processed_events_sqs_received_at
    on processed_events (sqs_received_at);