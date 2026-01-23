create table if not exists orders (
                                      id varchar(36) primary key,
    customer_code varchar(100) not null,
    amount integer not null,
    status varchar(30) not null,
    created_at timestamptz not null
    );