-- # -- !Ups

create table fitness_user_profile (
    user_id uuid not null constraint pk_fitness_user_profile primary key,
    height_in_cm decimal not null,
    weight_in_lbn decimal not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

-- # -- !Downs