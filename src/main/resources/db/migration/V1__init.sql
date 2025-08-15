create table users (
  id bigserial primary key,
  login varchar(255) not null unique,
  password_hash varchar(255) not null,
  created_at timestamp with time zone not null default current_timestamp
);

create table sessions (
  id uuid primary key,
  user_id bigint not null references users(id) on delete cascade,
  expires_at timestamp with time zone not null
);

create index idx_sessions_user_id on sessions(user_id);
create index idx_sessions_expires_at on sessions(expires_at);

create table locations (
  id bigserial primary key,
  user_id bigint not null references users(id) on delete cascade,
  name varchar(255) not null,
  latitude decimal(8,5) not null,
  longitude decimal(8,5) not null,
  created_at timestamp with time zone not null default current_timestamp
);

create index idx_locations_user_id on locations(user_id);
create index idx_locations_user_id_name on locations(user_id, name);