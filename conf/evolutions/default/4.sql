-- # -- !Ups

create table user_recently_viewed_workouts (
    id uuid not null constraint pk_user_recently_viewed_workouts primary key,
    user_id uuid not null,
    workout_id uuid not null,
    last_accessed timestamp not null default now(),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint unique_user_workout_id unique (user_id, workout_id)
);

-- # -- !Downs