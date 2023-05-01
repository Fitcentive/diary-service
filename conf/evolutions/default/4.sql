-- # -- !Ups

create table user_recently_viewed_workouts (
    user_id uuid not null,
    workout_id uuid not null,
    last_accessed timestamp not null default now(),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    primary key (user_id, workout_id)
);

-- # -- !Downs