-- # -- !Ups

create table fatsecret_food_cache (
    food_id varchar not null constraint pk_fatsecret_food_cache primary key,
    food_data jsonb,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

-- # -- !Downs