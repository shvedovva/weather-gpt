create table if not exists locations (
  id bigserial primary key,
  user_id bigint not null references users(id) on delete cascade,
  name varchar(255) not null,
  latitude decimal(8,5) not null,
  longitude decimal(8,5) not null,
  created_at timestamp with time zone not null default current_timestamp
);

create index if not exists idx_locations_user_id on locations(user_id);
create index if not exists idx_locations_user_id_name on locations(user_id, name);