-- # -- !Ups

create table user_recently_viewed_foods (
    id uuid not null constraint pk_user_recently_viewed_foods primary key,
    user_id uuid not null,
    food_id int not null,
    last_accessed timestamp not null default now(),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint unique_user_food_id unique (user_id, food_id)
);

-- # -- !Downs