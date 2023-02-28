-- # -- !Ups
--
-- To ensure UUIDs are autogenerated
create extension if not exists "uuid-ossp";

create table strength_workouts (
    id uuid not null constraint pk_strength_workouts primary key,
    user_id uuid not null,
    workout_id uuid not null,
    name varchar not null,
    exercise_date timestamp not null default now(),
    sets integer,
    reps integer,
    weight_in_lbs integer[] not null default array[]::integer[],
    calories_burned double,
    meetup_id uuid,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table cardio_workouts (
    id uuid not null constraint pk_cardio_workouts primary key,
    name varchar not null,
    cardio_date timestamp not null default now(),
    duration_in_minutes integer,
    calories_burned double,
    meetup_id uuid,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

-- # -- !Downs